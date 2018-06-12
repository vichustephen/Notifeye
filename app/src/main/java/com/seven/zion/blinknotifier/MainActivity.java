package com.seven.zion.blinknotifier;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements cameraPreview.FrameData {

    Button start;
    private Camera camera;
    private cameraPreview cameraP;
    int cameraRotation;
    FirebaseVisionImageMetadata metadata;
    TextView counts;
    FirebaseVisionFaceDetectorOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.preview);
        start = (Button)findViewById(R.id.button);
        counts = findViewById(R.id.count);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(getApplicationContext(),BackgroundService.class));
                //AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

            }
        });
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
       cameraRotation = setCameraDisplayOrientation(this,Camera.CameraInfo.CAMERA_FACING_FRONT,camera);
        cameraP = new cameraPreview(this,camera,this);
        frameLayout.addView(cameraP);
         options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                        .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .setTrackingEnabled(true)
                        .build();
        metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(1280)
                .setHeight(720)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(cameraRotation)
                .build();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera !=null)
            camera.release();
        camera = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera !=null)
            camera.release();
    }
    public static int setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        int CameraRotation;
        switch (result) {
            case 0:
                CameraRotation = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                CameraRotation = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                CameraRotation = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                CameraRotation = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                CameraRotation = FirebaseVisionImageMetadata.ROTATION_0;
                //Log.e(TAG, "Bad rotation value: " + rotationCompensation);
        }

        return  CameraRotation;
    }

    @Override
    public void sendFrames(byte[] frames) {

        Log.i("Frames" ,"Received");
        FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(frames,metadata);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
        Task<List<FirebaseVisionFace>> result = detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {

                for (FirebaseVisionFace faces : firebaseVisionFaces) {
                    if (faces.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                        float leftEyeOpenProb = faces.getLeftEyeOpenProbability();
                        counts.setText(String.format(Locale.getDefault(), "%f", leftEyeOpenProb));
                        Log.i("FACE","IM HERE");
                    }
                }
            }
        });

    }
}
