package com.seven.zion.blinknotifier;

import android.graphics.Bitmap;
import android.media.Image;

import com.google.firebase.ml.common.FirebaseMLException;

import java.nio.ByteBuffer;

public interface VisionImageProcessor {

  void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay)
      throws FirebaseMLException;
  void process(Bitmap bitmap, GraphicOverlay graphicOverlay);

  void process(Image bitmap, int rotation, GraphicOverlay graphicOverlay);

  void stop();
}
