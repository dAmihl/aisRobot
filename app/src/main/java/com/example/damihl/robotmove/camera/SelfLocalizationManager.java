package com.example.damihl.robotmove.camera;

import android.graphics.ColorMatrix;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.utils.RobotPosVector;
import com.example.damihl.robotmove.utils.WorldPoint;

import android.util.Log;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dAmihl on 14.05.15.
 */
public class SelfLocalizationManager {

    private static final String TAG = "SelfLocateMngr";

    private static SelfLocalizationManager instance = null;

    public static SelfLocalizationManager getInstance(){
        if (instance == null) instance = new SelfLocalizationManager();
        return instance;
    }



    private int BEACON_DETERMINATION_X_THRESHOLD = 50;


    private int DIFFERENT_COLOR_BATCH_X_DISTANCE = 100;
    private int COLOR_BATCH_MIN_SIZE = 20;
    private int DIFFERENT_COLOR_BATCH_Y_DISTANCE = 100;

    private int numberColorBatchesBlue = 0;
    private int numberColorBatchesRed = 0;
    private int numberColorBatchesYellow = 0;

    private ArrayList<Point> colorBatchesBlue = new ArrayList<>();
    private ArrayList<Point> colorBatchesRed = new ArrayList<>();
    private ArrayList<Point> colorBatchesYellow = new ArrayList<>();


    private ColorBlobDetector mDetectorBlue;
    private ColorBlobDetector mDetectorRed;
    private ColorBlobDetector mDetectorYellow;

    private SelfLocalizationManager(){
        this.mDetectorBlue = new ColorBlobDetector();
        this.mDetectorRed = new ColorBlobDetector();
        this.mDetectorYellow = new ColorBlobDetector();

        this.mDetectorRed.setHsvColor(CameraManager.getInstance().RED_COLOR);
        this.mDetectorBlue.setHsvColor(CameraManager.getInstance().BLUE_COLOR);
        this.mDetectorYellow.setHsvColor(CameraManager.getInstance().YELLOW_COLOR);
    }


    // names from upper to lower color
    private static final WorldPoint BEACON_BLUE_YELLOW_POSITION = new WorldPoint(-125, 125);
    private static final WorldPoint BEACON_BLUE_RED_POSITION = new WorldPoint(0, 125);
    private static final WorldPoint BEACON_RED_YELLOW_POSITION = new WorldPoint(125, 125);

    private static final WorldPoint BEACON_WHITE_BLUE_POSITION = new WorldPoint(-125, 0);
    private static final WorldPoint BEACON_WHITE_RED_POSITION = new WorldPoint(125, 0);

    private static final WorldPoint BEACON_YELLOW_BLUE_POSITION = new WorldPoint(-125, -125);
    private static final WorldPoint BEACON_RED_BLUE_POSITION = new WorldPoint(0, -125);
    private static final WorldPoint BEACON_YELLOW_RED_POSITION = new WorldPoint(125, -125);


    private static enum BEACON{
        //from left to right
        // upper to lower color
        // given by exercise image on course homepage
        BLUE_YELLOW, BLUE_RED, RED_YELLOW,  // top
        WHITE_BLUE,             WHITE_RED,  // mid
        YELLOW_BLUE, RED_BLUE, YELLOW_RED,  // bottom
        UNDEFINED // used for error handling
    }


    public void beaconDetection(Mat mRgba){
        mDetectorBlue.process(mRgba);
        List<MatOfPoint> contoursBlue = mDetectorBlue.getContours();
        Imgproc.drawContours(mRgba, contoursBlue, -1, CameraManager.getInstance().getContourColor());

        mDetectorRed.process(mRgba);
        List<MatOfPoint> contoursRed = mDetectorRed.getContours();
        Imgproc.drawContours(mRgba, contoursRed, -1, CameraManager.getInstance().getContourColor());

        mDetectorYellow.process(mRgba);
        List<MatOfPoint> contoursYellow = mDetectorYellow.getContours();
        Imgproc.drawContours(mRgba, contoursYellow, -1, CameraManager.getInstance().getContourColor());

        int numContoursBlue = contoursBlue.size();
        int numContoursYellow = contoursYellow.size();
        int numContoursRed = contoursRed.size();


        Point[] middlePointsBlue = new Point[numContoursBlue];
        Point[] middlePointsRed = new Point[numContoursRed];
        Point[] middlePointsYellow = new Point[numContoursYellow];

        Log.i(TAG, "****BLUE BEACON DETECTION*****: NumContours: "+numContoursBlue);
        for (int i = 0; i < numContoursBlue; i++){
            middlePointsBlue[i] = getContoursMiddle(contoursBlue, i);
            Log.i(TAG, "Contour["+i+"] MiddlePoint: "+middlePointsBlue[i]);
        }

        Log.i(TAG, "****RED BEACON DETECTION*****: NumContours: "+numContoursRed);
        for (int i = 0; i < numContoursRed; i++){
            middlePointsRed[i] = getContoursMiddle(contoursRed, i);
            Log.i(TAG, "Contour["+i+"] MiddlePoint: "+middlePointsRed[i]);
        }

        Log.i(TAG, "****YELLOW BEACON DETECTION*****: NumContours: "+numContoursYellow);
        for (int i = 0; i < numContoursYellow; i++){
            middlePointsYellow[i] = getContoursMiddle(contoursYellow, i);
            Log.i(TAG, "Contour["+i+"] MiddlePoint: "+middlePointsYellow[i]);
        }

        Comparator<Point> pointComparatorX = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                if (lhs.x < rhs.x) return -1;
                else if (lhs.x > rhs.x) return 1;
                return 0;
            }
        };

        Arrays.sort(middlePointsRed, pointComparatorX);
        Arrays.sort(middlePointsBlue, pointComparatorX);
        Arrays.sort(middlePointsYellow, pointComparatorX);

        numberColorBatchesBlue = computeBatches(numContoursBlue, middlePointsBlue, colorBatchesBlue);
        numberColorBatchesRed = computeBatches(numContoursRed, middlePointsRed, colorBatchesRed);
        numberColorBatchesYellow = computeBatches(numContoursYellow, middlePointsYellow, colorBatchesYellow);

        Log.i(TAG, "Number Color Batches Blue: "+numberColorBatchesBlue);
        for (Point p : colorBatchesBlue){
            Log.i(TAG, p.x+"/"+p.y);
        }
        Log.i(TAG, "Number Color Batches Red: "+numberColorBatchesRed);
        for (Point p : colorBatchesRed){
            Log.i(TAG, p.x+"/"+p.y);
        }
        Log.i(TAG, "Number Color Batches Yellow: "+numberColorBatchesYellow);
        for (Point p : colorBatchesYellow){
            Log.i(TAG, p.x+"/"+p.y);
        }

        determineBeacons(colorBatchesBlue, colorBatchesRed, colorBatchesYellow);

    }


    private void determineBeacons(ArrayList<Point> batchesBlue, ArrayList<Point> batchesRed, ArrayList<Point> batchesYellow){

        BEACON leftBeacon = determineLeftBeaconInScreen(batchesBlue, batchesRed, batchesYellow);
        BEACON rightBeacon = determineRightBeaconInScreen(batchesBlue, batchesRed, batchesYellow);

        if (leftBeacon == BEACON.UNDEFINED || rightBeacon == BEACON.UNDEFINED){
            Log.i(TAG, "At least one beacon could not be determined!");
            return;
        }

        Log.i(TAG, "BEACONS DETERMINED: "+leftBeacon+" AND "+rightBeacon);


    }

    private BEACON determineLeftBeaconInScreen(ArrayList<Point> batchesBlue, ArrayList<Point> batchesRed, ArrayList<Point> batchesYellow){
        BEACON leftBeacon = BEACON.UNDEFINED;

        Point minimumBlueX = getMinimumXFromBatches(batchesBlue);
        Point minimumRedX = getMinimumXFromBatches(batchesRed);
        Point minimumYellowX = getMinimumXFromBatches(batchesYellow);

        if (minimumBlueX == null || minimumRedX == null || minimumYellowX == null){
            return BEACON.UNDEFINED;
        }

        if (Math.abs(minimumBlueX.x - minimumRedX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (minimumBlueX.y < minimumRedX.y) leftBeacon = BEACON.BLUE_RED;
            else leftBeacon = BEACON.RED_BLUE;
        }

        if (Math.abs(minimumBlueX.x - minimumYellowX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (minimumBlueX.y < minimumYellowX.y) leftBeacon = BEACON.BLUE_YELLOW;
            else leftBeacon = BEACON.YELLOW_BLUE;
        }

        if (Math.abs(minimumRedX.x - minimumYellowX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (minimumRedX.y < minimumYellowX.y) leftBeacon = BEACON.RED_YELLOW;
            else leftBeacon = BEACON.YELLOW_RED;
        }
        return leftBeacon;
    }


    private BEACON determineRightBeaconInScreen(ArrayList<Point> batchesBlue, ArrayList<Point> batchesRed, ArrayList<Point> batchesYellow){
        BEACON leftBeacon = BEACON.UNDEFINED;

        Point maximumBlueX = getMaximumXFromBatches(batchesBlue);
        Point maximumRedX = getMaximumXFromBatches(batchesRed);
        Point maximumYellowX = getMaximumXFromBatches(batchesYellow);

        if (maximumBlueX == null || maximumRedX == null || maximumYellowX == null){
            return BEACON.UNDEFINED;
        }

        if (Math.abs(maximumBlueX.x - maximumRedX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (maximumBlueX.y < maximumRedX.y) leftBeacon = BEACON.BLUE_RED;
            else leftBeacon = BEACON.RED_BLUE;
        }

        if (Math.abs(maximumBlueX.x - maximumYellowX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (maximumBlueX.y < maximumYellowX.y) leftBeacon = BEACON.BLUE_YELLOW;
            else leftBeacon = BEACON.YELLOW_BLUE;
        }

        if (Math.abs(maximumRedX.x - maximumYellowX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (maximumRedX.y < maximumYellowX.y) leftBeacon = BEACON.RED_YELLOW;
            else leftBeacon = BEACON.YELLOW_RED;
        }
        return leftBeacon;
    }

    private Point getMinimumXFromBatches(ArrayList<Point> batches){
        if (batches.isEmpty()) return null;
         Point result = batches.get(0);
        for (int i = 1; i < batches.size(); i++){
            if (result.x > batches.get(i).x){
                result = batches.get(i);
            }
        }
        return result;
    }

    private Point getMaximumXFromBatches(ArrayList<Point> batches){
        if (batches.isEmpty()) return null;
        Point result = batches.get(0);
        for (int i = 1; i < batches.size(); i++){
            if (result.x < batches.get(i).x){
                result = batches.get(i);
            }
        }
        return result;
    }


    private int computeBatches(int numContours, Point[] middlePoints, ArrayList<Point> batches ){
        int numberColorBatches = 0;
        batches.clear();

        if (numContours > 0) {

            Point start = new Point();
            start.x = middlePoints[0].x;
            start.y = middlePoints[0].y;
            batches.add(start);

            numberColorBatches = 1;

                for (int i = 0; i < numContours - 1; i++) {
                    if (Math.abs(middlePoints[i].x - middlePoints[i+1].x) > DIFFERENT_COLOR_BATCH_X_DISTANCE ) {
                        numberColorBatches++;
                        Point p = new Point();
                        p.x = (middlePoints[i+1].x);
                        p.y = (middlePoints[i+1].y);
                        batches.add(p);
                    }else{
                        Point p = new Point();
                        p.x = (middlePoints[i].x + middlePoints[i+1].x) / 2;
                        p.y = (middlePoints[i].y + middlePoints[i+1].y) / 2;

                        if (!batches.isEmpty()){
                            Point oldP = batches.get(batches.size() - 1);
                            if (Math.abs(oldP.x - p.x) < DIFFERENT_COLOR_BATCH_X_DISTANCE){
                                oldP.x = (oldP.x + p.x)/2;
                                oldP.y = (oldP.y + p.y)/2;
                            }else{
                                batches.add(p);
                            }
                        }else{
                            batches.add(p);
                        }
                    }
                }
            }

        return numberColorBatches;
    }


    private Point getContoursMiddle(List<MatOfPoint> contours, int index){
            Point centroid = new Point(0, 0);
            for (Point p : contours.get(index).toArray()) {
                centroid.x += p.x;
                centroid.y += p.y;
            }
            centroid.x /= contours.get(index).toArray().length;
            centroid.y /= contours.get(index).toArray().length;

            return centroid;

    }





    public RobotPosVector computeMyLocation(){
        RobotPosVector myPos = new RobotPosVector(0,0,0);

        int numContoursBlue = CameraManager.getInstance().countNumberOfContours(CameraManager.getInstance().BLUE_COLOR);
        int numContoursRed = CameraManager.getInstance().countNumberOfContours(CameraManager.getInstance().RED_COLOR);
        int numContoursYellow = CameraManager.getInstance().countNumberOfContours(CameraManager.getInstance().YELLOW_COLOR);




        // compute the stuff
        return myPos;
    }




}
