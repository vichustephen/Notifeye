package com.seven.zion.blinknotifier;

import android.graphics.Bitmap;
import android.media.Image;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
public abstract class VisionProcessorBase<T> implements VisionImageProcessor {
    private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);

    public VisionProcessorBase() {
    }

    @Override
    public void process(
            ByteBuffer data, final FrameMetadata frameMetadata, final GraphicOverlay
            graphicOverlay) {
        if (shouldThrottle.get()) {
            return;
        }
        FirebaseVisionImageMetadata metadata =
                new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frameMetadata.getWidth())
                        .setHeight(frameMetadata.getHeight())
                        .setRotation(frameMetadata.getRotation())
                        .build();

        detectInVisionImage(
                FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata, graphicOverlay);
    }

    @Override
    public void process(Bitmap bitmap, final GraphicOverlay
            graphicOverlay) {
        if (shouldThrottle.get()) {
            return;
        }
        detectInVisionImage(FirebaseVisionImage.fromBitmap(bitmap), null, graphicOverlay);
    }

    @Override
    public void process(Image image, int rotation, final GraphicOverlay graphicOverlay) {
        if (shouldThrottle.get()) {
            return;
        }
        // This is for overlay display's usage
        FrameMetadata frameMetadata =
                new FrameMetadata.Builder().setWidth(image.getWidth()).setHeight(image.getHeight
                        ()).build();
        FirebaseVisionImage fbVisionImage =
                FirebaseVisionImage.fromMediaImage(image, rotation);
        detectInVisionImage(fbVisionImage, frameMetadata, graphicOverlay);
    }

    private void detectInVisionImage(
            FirebaseVisionImage image,
            final FrameMetadata metadata,
            final GraphicOverlay graphicOverlay) {
        detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<T>() {
                            @Override
                            public void onSuccess(T results) {
                                shouldThrottle.set(false);
                                VisionProcessorBase.this.onSuccess(results, metadata,
                                        graphicOverlay);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                shouldThrottle.set(false);
                                VisionProcessorBase.this.onFailure(e);
                            }
                        });
        // Begin throttling until this frame of input has been processed, either in onSuccess or
        // onFailure.
        shouldThrottle.set(true);
    }

    @Override
    public void stop() {
    }

    protected abstract Task<T> detectInImage(FirebaseVisionImage image);

    protected abstract void onSuccess(
            @NonNull T results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    protected abstract void onFailure(@NonNull Exception e);
}
