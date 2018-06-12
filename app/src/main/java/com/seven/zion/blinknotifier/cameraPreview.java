package com.seven.zion.blinknotifier;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

public class cameraPreview extends SurfaceView implements SurfaceHolder.Callback,Camera.PreviewCallback{

    public interface FrameData
    {
        void sendFrames(byte[] frames);
    }

    private  SurfaceHolder surfaceHolder;
    private Camera camera;
    FrameData frameInterface;

    public cameraPreview(Context context, Camera camera ,FrameData frameInterface) {
        super(context);
        this.camera = camera;
        this.frameInterface = frameInterface;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            camera.setPreviewCallback(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

        frameInterface.sendFrames(bytes);
      //  Log.i("sending frames","sent");
    }
}
