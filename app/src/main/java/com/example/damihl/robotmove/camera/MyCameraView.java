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
            setPreviewSize(resolution);
            disconnectCamera();
            mMaxHeight = resolution.height;
            mMaxWidth = resolution.width;
            connectCamera(getWidth(), getHeight());
        }

        public void setPreviewSize(Size res){
            Camera.Parameters params = mCamera.getParameters();
            params.setPreviewSize(res.width, res.height);
            mCamera.setParameters(params);
        }

        public Size getResolution() {
            return mCamera.getParameters().getPreviewSize();
        }

        public Size getBestSize(){
            android.hardware.Camera.Size maxSize = getResolutionList().get(0);
            for (android.hardware.Camera.Size s: getResolutionList()){
                Log.i(TAG, "SUPPORTED RESOLUTIONS: "+s.width + "/"+s.height);
                if (s.width > maxSize.width || s.height > maxSize.height) maxSize = s;
            }
            return maxSize;
        }

    @Override
    protected boolean connectCamera(int width, int height) {
        //Size bestSize = getBestSize();
        return super.connectCamera(800,480);

    }


}
