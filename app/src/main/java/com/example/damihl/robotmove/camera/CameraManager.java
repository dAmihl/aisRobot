package com.example.damihl.robotmove.camera;

import android.util.Log;
import android.view.SurfaceView;

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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

/**
 * Created by dAmihl on 26.04.15.
 */
public class CameraManager implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static CameraManager instance = null;

    private int sleepTime = 100;

    private Thread camThread;

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

    private static final int COLOR_CHECK_RECTANGLE_SIZE = 6;

    private boolean FOUND_COLOR_IN_IMAGE = false;
    private int LAST_FOUND_COLOR_POS_X = 0;
    private int LAST_FOUND_COLOR_POS_Y = 0;

    /*
    Set HSV boundarys for color detection
     */
    private static int iLowH = 38;
    private static int iHighH = 75;

    private static int iLowS = 100;
    private static int iHighS = 255;

    private static int iLowV = 0;
    private static int iHighV = 255;


    /*
    Define screen mid to check for green color in mid
     */
    private static int SCREEN_MID_X = 200;
    private static int SCREEN_MID_Y = 200;
    private static int SCREEN_MID_OFFSET = 50;



    public static CameraManager getInstance(){
        if (instance == null) instance = new CameraManager();
        return instance;
    }


    private void init(){
        mOpenCvCameraView = (CameraBridgeViewBase) MainActivity.getInstance().findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableView();
    }


    public void asyncLoadCamera(){
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, MainActivity.getInstance(), mLoaderCallback);
    }


    private CameraManager(){
        setUpBaseLoaderCallback();
    }

    public void setUpBaseLoaderCallback(){
        mLoaderCallback = new BaseLoaderCallback(MainActivity.getInstance()) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                    {
                        Log.i("CAM", "OpenCV loaded successfully");
                        init();
                        //mOpenCvCameraView.enableView();
                    } break;
                    default:
                    {
                        Log.d("CAM", "no success at loading opencv");
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
        processImage();
        if (checkColorInMiddle()){
            Log.i("CAM", "CAM FOUND COLOR IN MIDDLE");
        }
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


        Log.i("CAM", "camera view started with w:"+width+"/h:"+height);
        startCameraManager(ControlManager.getInstance(), MainActivity.getInstance());
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (currentFrame == null)
            currentFrame = inputFrame.rgba();
        return mRgba;
    }



    public boolean checkColorInMiddle(){

       if (!FOUND_COLOR_IN_IMAGE) return false;

        // only y coordinate measures the angle to the object
        // x coordinate measures the distance
       boolean isInMid =
               (LAST_FOUND_COLOR_POS_Y <= SCREEN_MID_Y + SCREEN_MID_OFFSET) &&
               (LAST_FOUND_COLOR_POS_Y >= SCREEN_MID_Y - SCREEN_MID_OFFSET)/* &&
               (LAST_FOUND_COLOR_POS_X <= SCREEN_MID_X + SCREEN_MID_OFFSET) &&
               (LAST_FOUND_COLOR_POS_X >= SCREEN_MID_X - SCREEN_MID_OFFSET)
               */;

        return isInMid;
    }



    public void processImage(){
        if (currentFrame == null) return;


        Mat imgHSV = new Mat();
        Imgproc.cvtColor(currentFrame, imgHSV, Imgproc.COLOR_RGB2HSV);

        Mat imgThresholded = new Mat();
        Core.inRange(imgHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), imgThresholded);


        Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
        Imgproc.dilate(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));

        //morphological closing (fill small holes in the foreground)
        Imgproc.dilate(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
        Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));


        Moments oMoments = Imgproc.moments(imgThresholded);

        double dM01 = oMoments.get_m01();
        double dM10 = oMoments.get_m10();
        double dArea = oMoments.get_m00();

        if (dArea > 10000) {
            //calculate the position of the ball
            double posX = dM10 / dArea;
            double posY = dM01 / dArea;
           // Log.i("CAM", "CAM FOUND SOMETHING GREEN AT "+posX+"/"+posY);
            Core.circle(imgThresholded, new Point(posX, posY),20,new Scalar(100,100,100));
            LAST_FOUND_COLOR_POS_X = (int) posX;
            LAST_FOUND_COLOR_POS_Y = (int) posY;
            FOUND_COLOR_IN_IMAGE = true;
        }else{
            FOUND_COLOR_IN_IMAGE = false;
        }


        // Imgproc.cvtColor(imgThresholded, mRgba, Imgproc.COLOR_HSV2RGB_FULL);
        Mat rgb = new Mat();
        Imgproc.cvtColor(imgThresholded, rgb,Imgproc.COLOR_GRAY2RGB);
        Mat rgba = new Mat();
        Imgproc.cvtColor(rgb, mRgba, Imgproc.COLOR_RGB2RGBA, 0);
        currentFrame = null;

        rgb.release();
        rgba.release();
        imgHSV.release();
        imgThresholded.release();

    }
}
