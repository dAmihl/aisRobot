package com.example.damihl.robotmove.camera;

import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.R;
import com.example.damihl.robotmove.controls.ControlManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.List;

/**
 * Created by dAmihl on 26.04.15.
 */
public class CameraManager implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static CameraManager instance = null;
    private static final String TAG = "CAMMNGR";
    private int sleepTime = 100;

    private Thread camThread;

    private boolean mIsColorSelected = false;
    private CameraBridgeViewBase mOpenCvCameraView;
    private BaseLoaderCallback mLoaderCallback;
    private Mat currentFrame;
    private Mat mRgba;
    private Scalar mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector mDetector;
    private Mat                  mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;


    public Scalar GREEN_COLOR = new Scalar(107.78125, 255.0, 127.546875, 0.0);
    public Scalar RED_COLOR = new Scalar(250.484375, 197.3125, 249.765625, 0.0);
    public Scalar BLUE_COLOR = new Scalar(146.453125, 241.765625, 228.828125, 0.0);


    private static final int COLOR_CHECK_RECTANGLE_SIZE = 6;

    private boolean FOUND_COLOR_IN_IMAGE = false;
    private int LAST_FOUND_COLOR_POS_X = 0;
    private int LAST_FOUND_COLOR_POS_Y = 0;

    private int CIRCLE_CONTOUR_RADIUS = 0;
    /*
    Set HSV boundarys for color detection
     */
    private static int iLowH = 100;
    private static int iHighH = 120;

    private static int iLowS = 200;
    private static int iHighS = 255;

    private static int iLowV = 100;
    private static int iHighV = 120;


    /*
    Define screen mid to check for green color in mid
     */
    private static int SCREEN_MID_X = 150;
    private static int SCREEN_MID_Y = 200;
    private static int SCREEN_MID_OFFSET = 20;



    public static CameraManager getInstance(){
        if (instance == null) instance = new CameraManager();
        return instance;
    }


    private void init(){
        mOpenCvCameraView = (CameraBridgeViewBase) MainActivity.getInstance().findViewById(R.id.color_blob_detection_activity_surface_view);
        if (mOpenCvCameraView == null) {
            return;
        }
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(this);
        mOpenCvCameraView.enableView();

    }


    public void asyncLoadCamera(){
        setUpBaseLoaderCallback();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, MainActivity.getInstance(), mLoaderCallback);
    }


    private CameraManager(){

    }

    public void setUpBaseLoaderCallback(){
        mLoaderCallback = new BaseLoaderCallback(MainActivity.getInstance()) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                    {
                        Log.i(TAG, "OpenCV loaded successfully");
                        init();
                        //mOpenCvCameraView.enableView();
                    } break;
                    default:
                    {
                        Log.d(TAG, "no success at loading opencv");
                        super.onManagerConnected(status);
                    } break;
                }
            }
        };
    }


    public void joinThread() throws InterruptedException {
        if (this.camThread != null)
            this.camThread.join();
    }

    public void initCamThread(final MainActivity appl, final ControlManager control){
        camThread = new Thread(new Runnable(){

            @Override
            public void run() {
                while(true) {
                    update();
                    try {
                        Thread.sleep(sleepTime);
                    } catch (Exception e) {
                        appl.threadSafeDebugOutput(e.toString());
                    }
                }
            }

        });
    }

    public void startCameraManager(final ControlManager control, final MainActivity appl){
        try {
            camThread.start();
        }catch(Exception e){
            appl.threadSafeDebugOutput("camera manager error: "+e);
        }
        appl.threadSafeDebugOutput("camera manager started");
    }

    public void update(){
        if (checkColorInMiddle()){
            Log.i(TAG, "CAM FOUND COLOR IN MIDDLE");
        }
        if (checkColorInRange()){
            Log.i(TAG, "COLOR BLOB IN RANGE NOW!!");
        };
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);


        Log.i(TAG, "camera view started with w:"+width+"/h:"+height);
        startCameraManager(ControlManager.getInstance(), MainActivity.getInstance());
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        onCameraFrame2(inputFrame);
        return mRgba;
    }



    public boolean checkColorInMiddle(){

       boolean isInMid =
               (LAST_FOUND_COLOR_POS_X <= SCREEN_MID_X + SCREEN_MID_OFFSET) &&
               (LAST_FOUND_COLOR_POS_X >= SCREEN_MID_X - SCREEN_MID_OFFSET)/* &&
               (LAST_FOUND_COLOR_POS_X <= SCREEN_MID_X + SCREEN_MID_OFFSET) &&
               (LAST_FOUND_COLOR_POS_X >= SCREEN_MID_X - SCREEN_MID_OFFSET)
               */;

        Log.i(TAG, "X: "+LAST_FOUND_COLOR_POS_X+ "IS IN "+SCREEN_MID_X+"?");

        return isInMid;
    }

    public boolean checkColorInRange(){


        return 2*CIRCLE_CONTOUR_RADIUS > 65;

    }


    public void setColor(Scalar col){
        mDetector.setHsvColor(col);
        mIsColorSelected = true;
    }



    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();
        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;
        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
        Rect touchedRect = new Rect();
        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;
        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
        Mat touchedRegionRgba = mRgba.submat(touchedRect);
        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
// Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;
        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
        Log.i(TAG, "TOUCHED HSV COLOR: "+mBlobColorHsv);
        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");
        mDetector.setHsvColor(mBlobColorHsv);
        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
        mIsColorSelected = true;
        touchedRegionRgba.release();
        touchedRegionHsv.release();
        return false; // don't need subsequent touch events
    }



    public Mat onCameraFrame2(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        if(mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
           // Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);
            if (contours.size() == 1) {
                Point centroid = new Point(0, 0);
                for (Point p : contours.get(0).toArray()) {
                    centroid.x += p.x;
                    centroid.y += p.y;
                }
                centroid.x /= contours.get(0).toArray().length;
                centroid.y /= contours.get(0).toArray().length;

                LAST_FOUND_COLOR_POS_X = (int) centroid.x;
                LAST_FOUND_COLOR_POS_Y = (int) centroid.y;
                //Log.i(TAG, "X: "+LAST_FOUND_COLOR_POS_X+"/ Y: "+LAST_FOUND_COLOR_POS_Y);

                Double max = new Double(0);
                for (Point p : contours.get(0).toArray()) {
                    double tmp = (centroid.x-p.x)*(centroid.x-p.x) + (centroid.y-p.y)*(centroid.y-p.y);
                    if(tmp > max)
                    {
                        max = tmp;
                    }
                }
                CIRCLE_CONTOUR_RADIUS = (int)Math.sqrt(max);
                Core.circle(mRgba, centroid, CIRCLE_CONTOUR_RADIUS, CONTOUR_COLOR);
            }
            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);
            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }
        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
        return new Scalar(pointMatRgba.get(0, 0));
    }
}
