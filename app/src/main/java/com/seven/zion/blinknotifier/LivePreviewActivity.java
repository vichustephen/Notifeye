package com.seven.zion.blinknotifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

public final class LivePreviewActivity extends AppCompatActivity implements FaceDetectionProcessor.countDetect,
        OnRequestPermissionsResultCallback{
    private static final String FACE_DETECTION = "Face Detection";
    private static final String TAG = "LivePreviewActivity";
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = FACE_DETECTION;
    float Height,Width;
    private Button countsView,skip;
    private TextView toGo;
    private int Originalcount = 0,givenCount = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_live_preview);
        preview = (CameraSourcePreview) findViewById(R.id.firePreview);
        skip = (Button)findViewById(R.id.skip);
        toGo = (TextView)findViewById(R.id.toGo);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = (GraphicOverlay) findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
        countsView = (Button) findViewById(R.id.counts);
        createCameraSource(selectedModel);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LivePreviewActivity.this.finish();
            }
        });

    }


    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            float density = getResources().getDisplayMetrics().density;
            Height = outMetrics.heightPixels;
            Width = outMetrics.widthPixels;
            cameraSource = new CameraSource(this, graphicOverlay,Height,Width);
            cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
        }

        try {
            switch (model) {
                case FACE_DETECTION:
                    Log.i(TAG, "Using Face Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(this));
                    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
        } catch (Exception e) {
            Log.e(TAG, "can not create camera source: " + model);
        }
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay,Height,Width);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
            sendBroadcast(new Intent("stopService").putExtra("real",true));
        }
    }
    @Override
    public void onCountDetected() {
        Originalcount++;
        countsView.setText(String.format(Locale.getDefault(),"%d",Originalcount));
        if (Originalcount >= givenCount)
            LivePreviewActivity.this.finish();

    }
}
