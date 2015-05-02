package com.example.damihl.robotmove.camera;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.util.List;


import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by dAmihl on 02.05.15.
 */
public class MyCameraView  extends JavaCameraView {

        private static final String TAG = "Sample::Tutorial3View";

        public MyCameraView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public List<String> getEffectList() {
            return mCamera.getParameters().getSupportedColorEffects();
        }

        public boolean isEffectSupported() {
            return (mCamera.getParameters().getColorEffect() != null);
        }

        public String getEffect() {
            return mCamera.getParameters().getColorEffect();
        }

        public void setEffect(String effect) {
            Camera.Parameters params = mCamera.getParameters();
            params.setColorEffect(effect);
            mCamera.setParameters(params);
        }

        public List<Size> getResolutionList() {
            return mCamera.getParameters().getSupportedPreviewSizes();
        }

        public void setResolution(Size resolution) {
            disconnectCamera();
            mMaxHeight = resolution.height;
            mMaxWidth = resolution.width;
            connectCamera(getWidth(), getHeight());
        }

        public Size getResolution() {
            return mCamera.getParameters().getPreviewSize();
        }




}
