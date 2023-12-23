package com.seven.zion.blinknotifier;

import androidx.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {
    public  interface countDetect{
        void onCountDetected();
    }
 countDetect detects;
  private static final String TAG = "FaceDetectionProcessor";

  private final FirebaseVisionFaceDetector detector;
    public int count = 0;
    double lastFrame = 0.99;

  public FaceDetectionProcessor(countDetect detects) {
      this.detects = detects;
    FirebaseVisionFaceDetectorOptions options =
        new FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setContourMode(1)
            .build();

    detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
  }

  @Override
  public void stop() {
    try {
      detector.close();
    } catch (IOException e) {
      Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
    }
  }

  @Override
  protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
    return detector.detectInImage(image);
  }

  @Override
  protected void onSuccess(
      @NonNull List<FirebaseVisionFace> faces,
      @NonNull FrameMetadata frameMetadata,
      @NonNull GraphicOverlay graphicOverlay) {
    graphicOverlay.clear();
    for (int i = 0; i < faces.size(); ++i) {
      FirebaseVisionFace face = faces.get(i);
      FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay);
      graphicOverlay.add(faceGraphic);
      faceGraphic.updateFace(face, frameMetadata.getCameraFacing(),count);
      Log.i("Probability ",String.format(Locale.getDefault(),"%f",face.getLeftEyeOpenProbability()));
     // if (lastFrame >0.80 && face.getRightEyeOpenProbability() <0)
    //      detects.onCountDetected();
      if (face.getRightEyeOpenProbability()<0.70 && face.getRightEyeOpenProbability()>0 &&
              face.getRightEyeOpenProbability() !=lastFrame && lastFrame >0.80){
          count++;
          if (count>=3) {
              detects.onCountDetected();
              count =0;
              lastFrame = face.getRightEyeOpenProbability();
          }
      }
      else {
          count = 0;
          lastFrame = face.getRightEyeOpenProbability();
      }
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }
}
