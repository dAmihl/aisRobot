package com.example.damihl.robotmove.camera;

import com.example.damihl.robotmove.utils.RobotPosVector;
import com.example.damihl.robotmove.utils.WorldPoint;

import android.support.v4.util.Pair;
import android.util.Log;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private boolean MY_POSITION_DETERMINED = false;
    private RobotPosVector robotPosition = new RobotPosVector(0,0,0);


    private int BEACON_DETERMINATION_X_THRESHOLD = 50;


    private int DIFFERENT_COLOR_BATCH_X_DISTANCE = 100;
    private int COLOR_BATCH_MIN_SIZE = 10;
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


    public static enum BEACON_TYPE {
        //from left to right
        // upper to lower color
        // given by exercise image on course homepage
        BLUE_YELLOW, BLUE_RED, RED_YELLOW,  // top
        WHITE_BLUE,             WHITE_RED,  // mid
        YELLOW_BLUE, RED_BLUE, YELLOW_RED,  // bottom
        UNDEFINED // used for error handling
    }



    private List<MatOfPoint> filterTooSmallContours(List<MatOfPoint> contours){
        List<MatOfPoint> newContours = new ArrayList<MatOfPoint>();
        for (MatOfPoint m: contours){
            if (m.size().area() > COLOR_BATCH_MIN_SIZE){
                newContours.add(m);
            }
        }
        return newContours;
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


        contoursBlue = filterTooSmallContours(contoursBlue);
        contoursYellow = filterTooSmallContours(contoursYellow);
        contoursRed = filterTooSmallContours(contoursRed);

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

        Beacon leftBeacon = determineLeftBeaconInScreen(batchesBlue, batchesRed, batchesYellow);
        Beacon rightBeacon = determineRightBeaconInScreen(batchesBlue, batchesRed, batchesYellow);

        if (leftBeacon.getType() == BEACON_TYPE.UNDEFINED || rightBeacon.getType() == BEACON_TYPE.UNDEFINED
                || leftBeacon.getType() == rightBeacon.getType()){
            if (leftBeacon.getType() == rightBeacon.getType()){
                Log.i(TAG, "Left and Right beacon have the same type!");
            }
            if (leftBeacon.getType() == BEACON_TYPE.UNDEFINED){
                Log.i(TAG, "Left beacon could not be determined!");
            }else{
                Log.i(TAG, "Left beacon determined: "+leftBeacon.getType());
            }
            if (rightBeacon.getType() == BEACON_TYPE.UNDEFINED){
                Log.i(TAG, "Right beacon could not be determined!");
            }else{
                Log.i(TAG, "Right beacon determined: "+rightBeacon.getType());
            }
            Log.i(TAG, "At least one beacon could not be determined!");
            MY_POSITION_DETERMINED = false;
            return;
        }

        BEACON_TYPE leftType = leftBeacon.getType();
        BEACON_TYPE rightType = rightBeacon.getType();

        int robotPosQuadrant = 0;

        if (leftType == BEACON_TYPE.BLUE_RED && rightType == BEACON_TYPE.RED_YELLOW) { // +/+ quadrant
            robotPosQuadrant = 1;
            Log.i(TAG, "Quadrant of beacons is +X/+Y");
        }else if (leftType == BEACON_TYPE.BLUE_YELLOW && rightType == BEACON_TYPE.BLUE_RED){ // -/+ quadrant
            robotPosQuadrant = 2;
            Log.i(TAG, "Quadrant of beacons is -X/+Y");
        }else if (leftType == BEACON_TYPE.RED_BLUE && rightType == BEACON_TYPE.YELLOW_BLUE){ // -/- quadrant
            robotPosQuadrant = 3;
            Log.i(TAG, "Quadrant of beacons is -X/-Y");
        }else if (leftType == BEACON_TYPE.YELLOW_RED && rightType == BEACON_TYPE.RED_BLUE){ // +/- quadrant
            robotPosQuadrant = 4;
            Log.i(TAG, "Quadrant of beacons is +X/-Y");
        }else{
            Log.d(TAG, "BEACON COMBINATION NOT POSSIBLE!");
            MY_POSITION_DETERMINED = false;
            return ;
        }

        Log.i(TAG, "BEACONS DETERMINED: "+leftBeacon.getType()+" AND "+rightBeacon.getType());

        computeMyPosition(leftBeacon, rightBeacon, robotPosQuadrant);

    }

    private void computeMyPosition(Beacon leftBeacon, Beacon rightBeacon, int robotPosQuadrant){
        Point leftBottomBeaconPoint = leftBeacon.getBottomBeaconPoint();
        Point rightBottomBeaconPoint = rightBeacon.getBottomBeaconPoint();

        Log.i(TAG, "Left Beacon Point: "+leftBottomBeaconPoint.x+"/"+leftBottomBeaconPoint.y);
        Log.i(TAG, "Right Beacon Point: "+rightBottomBeaconPoint.x+"/"+rightBottomBeaconPoint.y);

        WorldPoint leftBeaconWorldPoint = determineBeaconWorldPoint(leftBeacon.getType());
        WorldPoint rightBeaconWorldPoint = determineBeaconWorldPoint(rightBeacon.getType());

        Point robocentricLeftBeaconPoint = HomographyManager.getInstance().getWorldCoordinatesInCentimeter(leftBottomBeaconPoint, CameraManager.getInstance().getMatrixHomography());
        Point robocentricRightBeaconPoint = HomographyManager.getInstance().getWorldCoordinatesInCentimeter(rightBottomBeaconPoint, CameraManager.getInstance().getMatrixHomography());

        WorldPoint robocentricLeftBeaconWorldPoint = new WorldPoint((float) robocentricLeftBeaconPoint.x, (float) robocentricLeftBeaconPoint.y);
        WorldPoint robocentricRightBeaconWorldPoint = new WorldPoint((float) robocentricRightBeaconPoint.x, (float) robocentricRightBeaconPoint.y);

        // switch to our coordinate system
        //robocentricLeftBeaconWorldPoint.switchOrientation();
        //robocentricRightBeaconWorldPoint.switchOrientation();

        Log.i(TAG, "Robocentric Left Beacon Homography: "+robocentricLeftBeaconWorldPoint);
        Log.i(TAG, "Robocentric Right Beacon Homography: "+robocentricRightBeaconWorldPoint);

        float pos1X = Math.max(leftBeaconWorldPoint.getX(), robocentricLeftBeaconWorldPoint.getX()) - Math.min(leftBeaconWorldPoint.getX(), robocentricLeftBeaconWorldPoint.getX());
        float pos1Y = Math.max(leftBeaconWorldPoint.getY(), robocentricLeftBeaconWorldPoint.getY()) - Math.min(leftBeaconWorldPoint.getY(), robocentricLeftBeaconWorldPoint.getY());
        float pos2X = Math.max(rightBeaconWorldPoint.getX(), robocentricRightBeaconWorldPoint.getX()) - Math.min(rightBeaconWorldPoint.getX(), robocentricRightBeaconWorldPoint.getX());
        float pos2Y = Math.max(rightBeaconWorldPoint.getY(), robocentricRightBeaconWorldPoint.getY()) - Math.min(rightBeaconWorldPoint.getY(), robocentricRightBeaconWorldPoint.getY());


        WorldPoint myPos1 = new WorldPoint(pos1X, pos1Y);
        WorldPoint myPos2 = new WorldPoint(pos2X, pos2Y);

        double angleBetweenBeacons = WorldPoint.angleBetween(robocentricLeftBeaconWorldPoint, robocentricRightBeaconWorldPoint);
        WorldPoint vGammaToLeftBeacon = new WorldPoint(robocentricLeftBeaconWorldPoint.getX() - robocentricRightBeaconWorldPoint.getX()
                ,robocentricLeftBeaconWorldPoint.getY() - robocentricRightBeaconWorldPoint.getY());
        WorldPoint vGammaToRobot = new WorldPoint(- robocentricRightBeaconWorldPoint.getX(), -robocentricRightBeaconWorldPoint.getY());
        double angleGamma = WorldPoint.angleBetween(vGammaToRobot, vGammaToLeftBeacon);

        double angleAlpha1 = WorldPoint.angleBetween(new WorldPoint(0,1), robocentricRightBeaconWorldPoint);

        double robotAngle = angleAlpha1 + angleGamma;

        //negative y quadrants
        if (robotPosQuadrant == 3 || robotPosQuadrant == 4){
            robotAngle += 180;
            robotAngle = robotAngle % 360;
        }

        Log.i(TAG, "My Position in world from left: "+myPos1);
        Log.i(TAG, "My Position in world from right: "+myPos2);
        Log.i(TAG, "Angle Between Beacons: "+angleBetweenBeacons);
        Log.i(TAG, "Angle GAMMA: "+angleGamma);
        Log.i(TAG, "Angle ALPHA1: "+angleAlpha1);
        Log.i(TAG, "Angle ROBOT: "+robotAngle);
       // Log.i(TAG, "With a offset off ("+(myPos1.getX() - myPos2.getX())+"/"+(myPos1.getY() - myPos2.getY()));


        WorldPoint myPos = myPos1;
        // switch to robot orientation
        myPos.switchOrientation();
        float robotOrientationAngle = ((float) robotAngle - 90);
        ///IMPORTANT! IT ALSO CAN BE HERE (90 - robotAngle) i am not sure about robots orientation here!
        if (robotOrientationAngle < 0) robotOrientationAngle += 360;

        MY_POSITION_DETERMINED = true;
        robotPosition = new RobotPosVector(myPos.getX(), myPos.getY(), robotOrientationAngle);

    }

    private WorldPoint determineBeaconWorldPoint(BEACON_TYPE type){
        WorldPoint result;

        switch (type){
            case BLUE_RED:
                result = BEACON_BLUE_RED_POSITION; break;
            case BLUE_YELLOW:
                result = BEACON_BLUE_YELLOW_POSITION;break;
            case RED_BLUE:
                result = BEACON_RED_BLUE_POSITION;break;
            case RED_YELLOW:
                result = BEACON_RED_YELLOW_POSITION;break;
            case YELLOW_BLUE:
                result = BEACON_YELLOW_BLUE_POSITION; break;
            case YELLOW_RED:
                result = BEACON_YELLOW_RED_POSITION;break;
            default:
                result = new WorldPoint(0,0);break;
        }
        return result;
    }

    private Beacon determineLeftBeaconInScreen(ArrayList<Point> batchesBlue, ArrayList<Point> batchesRed, ArrayList<Point> batchesYellow){
        Beacon leftBeacon = new Beacon(BEACON_TYPE.UNDEFINED);


        // each color has to appear once to determine a beacon
        if (batchesBlue.size() == 0 || batchesRed.size() == 0 || batchesYellow.size() == 0){
            leftBeacon.setType(BEACON_TYPE.UNDEFINED);
            Log.i(TAG, "One batch is empty!");
            //return rightBeacon;
        }


        ArrayList<Pair<Point, Point>> candidates = computeBeaconCandidatesFromBatches(batchesBlue, batchesRed, batchesYellow);
        Log.i(TAG, "BEACON CANDIDATES LEFT: ");
        if (candidates.size() == 0){
            Log.i(TAG, "NO CANDIDATES FOUND!");
            leftBeacon.setType(BEACON_TYPE.UNDEFINED);
            return leftBeacon;
        }


        // the maximum candidate, so the one on most right in screenspace
        Pair<Point, Point> minimumCandidate = candidates.get(0);
        // since the x values of both points are in a given threshold, it is only necessary to check for one x value

        for (int i = 1; i < candidates.size(); i++){
            Log.i(TAG, "Candidate: ("+candidates.get(i).first.x+"/"+candidates.get(i).first.y+") and ("
                    +candidates.get(i).second.x + "/"+ candidates.get(i).second.y+")");
            if (minimumCandidate.first.x > candidates.get(i).first.x){
                minimumCandidate = candidates.get(i);
            }
        }


        // determine beacon type
        if (batchesBlue.contains(minimumCandidate.first)){
            if (batchesRed.contains(minimumCandidate.second)){
                if (minimumCandidate.first.y < minimumCandidate.second.y){
                    leftBeacon.setType(BEACON_TYPE.BLUE_RED);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    leftBeacon.setType(BEACON_TYPE.RED_BLUE);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
            if (batchesYellow.contains(minimumCandidate.second)){
                if (minimumCandidate.first.y < minimumCandidate.second.y){
                    leftBeacon.setType(BEACON_TYPE.BLUE_YELLOW);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    leftBeacon.setType(BEACON_TYPE.YELLOW_BLUE);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
        }

        if (batchesRed.contains(minimumCandidate.first)){
            if (batchesBlue.contains(minimumCandidate.second)){
                if (minimumCandidate.first.y < minimumCandidate.second.y){
                    leftBeacon.setType(BEACON_TYPE.RED_BLUE);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    leftBeacon.setType(BEACON_TYPE.BLUE_RED);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
            if (batchesYellow.contains(minimumCandidate.second)){
                if (minimumCandidate.first.y < minimumCandidate.second.y){
                    leftBeacon.setType(BEACON_TYPE.RED_YELLOW);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    leftBeacon.setType(BEACON_TYPE.YELLOW_RED);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
        }

        if (batchesYellow.contains(minimumCandidate.first)){
            if (batchesRed.contains(minimumCandidate.second)){
                if (minimumCandidate.first.y < minimumCandidate.second.y){
                    leftBeacon.setType(BEACON_TYPE.YELLOW_RED);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }else {
                    leftBeacon.setType(BEACON_TYPE.RED_YELLOW);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
            if (batchesBlue.contains(minimumCandidate.second)){
                if (minimumCandidate.first.y < minimumCandidate.second.y){
                    leftBeacon.setType(BEACON_TYPE.YELLOW_BLUE);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    leftBeacon.setType(BEACON_TYPE.BLUE_YELLOW);
                    double x = (minimumCandidate.first.x + minimumCandidate.second.x) / 2;
                    double y = (minimumCandidate.first.y + minimumCandidate.second.y) / 2;
                    leftBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
        }

        // old method
        /*Beacon leftBeacon = new Beacon(BEACON_TYPE.UNDEFINED);

        Point minimumBlueX = getMinimumXFromBatches(batchesBlue);
        Point minimumRedX = getMinimumXFromBatches(batchesRed);
        Point minimumYellowX = getMinimumXFromBatches(batchesYellow);

        if (minimumBlueX == null || minimumRedX == null || minimumYellowX == null){
            leftBeacon.setType(BEACON_TYPE.UNDEFINED);
            return leftBeacon;
        }

        if (Math.abs(minimumBlueX.x - minimumRedX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (minimumBlueX.y < minimumRedX.y){
                leftBeacon.setType(BEACON_TYPE.BLUE_RED);
                leftBeacon.getBottomBeaconPoint().x = (minimumBlueX.x + minimumRedX.x)/2;
                leftBeacon.getBottomBeaconPoint().y = minimumRedX.y;
            }
            else {
                leftBeacon.setType(BEACON_TYPE.RED_BLUE);
                leftBeacon.getBottomBeaconPoint().x = (minimumBlueX.x + minimumRedX.x)/2;
                leftBeacon.getBottomBeaconPoint().y = minimumBlueX.y;
            }
        }

        if (Math.abs(minimumBlueX.x - minimumYellowX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (minimumBlueX.y < minimumYellowX.y){
                leftBeacon.setType(BEACON_TYPE.BLUE_YELLOW);
                leftBeacon.getBottomBeaconPoint().x = (minimumBlueX.x + minimumYellowX.x)/2;
                leftBeacon.getBottomBeaconPoint().y = minimumYellowX.y;
            }
            else{
                leftBeacon.setType(BEACON_TYPE.YELLOW_BLUE);
                leftBeacon.getBottomBeaconPoint().x = (minimumBlueX.x + minimumYellowX.x)/2;
                leftBeacon.getBottomBeaconPoint().y = minimumBlueX.y;
            }
        }

        if (Math.abs(minimumRedX.x - minimumYellowX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (minimumRedX.y < minimumYellowX.y){
                leftBeacon.setType(BEACON_TYPE.RED_YELLOW);
                leftBeacon.getBottomBeaconPoint().x = (minimumRedX.x + minimumYellowX.x)/2;
                leftBeacon.getBottomBeaconPoint().y = minimumYellowX.y;
            }
            else{
                leftBeacon.setType(BEACON_TYPE.YELLOW_RED);
                leftBeacon.getBottomBeaconPoint().x = (minimumRedX.x + minimumYellowX.x)/2;
                leftBeacon.getBottomBeaconPoint().y = minimumRedX.y;
            }
        }*/
        return leftBeacon;
    }


    private Beacon determineRightBeaconInScreen(ArrayList<Point> batchesBlue, ArrayList<Point> batchesRed, ArrayList<Point> batchesYellow){
        Beacon rightBeacon = new Beacon(BEACON_TYPE.UNDEFINED);


        // each color has to appear once to determine a beacon
        if (batchesBlue.size() == 0 || batchesRed.size() == 0 || batchesYellow.size() == 0){
            rightBeacon.setType(BEACON_TYPE.UNDEFINED);
            Log.i(TAG, "One batch is empty!");
            //return rightBeacon;
        }


        ArrayList<Pair<Point, Point>> candidates = computeBeaconCandidatesFromBatches(batchesBlue, batchesRed, batchesYellow);
        Log.i(TAG, "BEACON CANDIDATES RIGHT: ");
        if (candidates.size() == 0){
            Log.i(TAG, "NO CANDIDATES FOUND!");
            rightBeacon.setType(BEACON_TYPE.UNDEFINED);
            return rightBeacon;
        }

        // the maximum candidate, so the one on most right in screenspace
        Pair<Point, Point> maximumCandidate = candidates.get(0);
        // since the x values of both points are in a given threshold, it is only necessary to check for one x value

        for (int i = 1; i < candidates.size(); i++){
            Log.i(TAG, "Candidate: ("+candidates.get(i).first.x+"/"+candidates.get(i).first.y+") and ("
            +candidates.get(i).second.x + "/"+ candidates.get(i).second.y+")");
            if (maximumCandidate.first.x < candidates.get(i).first.x){
                maximumCandidate = candidates.get(i);
            }
        }




        // determine beacon type
        if (batchesBlue.contains(maximumCandidate.first)){
            if (batchesRed.contains(maximumCandidate.second)){
                if (maximumCandidate.first.y < maximumCandidate.second.y){
                    rightBeacon.setType(BEACON_TYPE.BLUE_RED);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    rightBeacon.setType(BEACON_TYPE.RED_BLUE);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
            if (batchesYellow.contains(maximumCandidate.second)){
                if (maximumCandidate.first.y < maximumCandidate.second.y){
                    rightBeacon.setType(BEACON_TYPE.BLUE_YELLOW);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    rightBeacon.setType(BEACON_TYPE.YELLOW_BLUE);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
        }

        if (batchesRed.contains(maximumCandidate.first)){
            if (batchesBlue.contains(maximumCandidate.second)){
                if (maximumCandidate.first.y < maximumCandidate.second.y){
                    rightBeacon.setType(BEACON_TYPE.RED_BLUE);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    rightBeacon.setType(BEACON_TYPE.BLUE_RED);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
            if (batchesYellow.contains(maximumCandidate.second)){
                if (maximumCandidate.first.y < maximumCandidate.second.y){
                    rightBeacon.setType(BEACON_TYPE.RED_YELLOW);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    rightBeacon.setType(BEACON_TYPE.YELLOW_RED);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
        }

        if (batchesYellow.contains(maximumCandidate.first)){
            if (batchesRed.contains(maximumCandidate.second)){
                if (maximumCandidate.first.y < maximumCandidate.second.y){
                    rightBeacon.setType(BEACON_TYPE.YELLOW_RED);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    rightBeacon.setType(BEACON_TYPE.RED_YELLOW);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
            if (batchesBlue.contains(maximumCandidate.second)){
                if (maximumCandidate.first.y < maximumCandidate.second.y){
                    rightBeacon.setType(BEACON_TYPE.YELLOW_BLUE);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }else{
                    rightBeacon.setType(BEACON_TYPE.BLUE_YELLOW);
                    double x = (maximumCandidate.first.x + maximumCandidate.second.x) / 2;
                    double y = (maximumCandidate.first.y + maximumCandidate.second.y) / 2;
                    rightBeacon.setBottomBeaconPoint(new Point(x, y));
                }
            }
        }

        // old method
      /*
        Point maximumBlueX = getMaximumXFromBatches(batchesBlue);
        Point maximumRedX = getMaximumXFromBatches(batchesRed);
        Point maximumYellowX = getMaximumXFromBatches(batchesYellow);

        if (maximumBlueX == null || maximumRedX == null || maximumYellowX == null){
            rightBeacon.setType(BEACON_TYPE.UNDEFINED);
            return rightBeacon;
        }

        if (Math.abs(maximumBlueX.x - maximumRedX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (maximumBlueX.y < maximumRedX.y){
                rightBeacon.setType(BEACON_TYPE.BLUE_RED);
                rightBeacon.getBottomBeaconPoint().x = (maximumBlueX.x + maximumRedX.x)/2;
                rightBeacon.getBottomBeaconPoint().y = maximumRedX.y;
            }
            else{
                rightBeacon.setType(BEACON_TYPE.RED_BLUE);
                rightBeacon.getBottomBeaconPoint().x = (maximumBlueX.x + maximumRedX.x)/2;
                rightBeacon.getBottomBeaconPoint().y = maximumBlueX.y;
            }
        }

        if (Math.abs(maximumBlueX.x - maximumYellowX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (maximumBlueX.y < maximumYellowX.y){
                rightBeacon.setType(BEACON_TYPE.BLUE_YELLOW);
                rightBeacon.getBottomBeaconPoint().x = (maximumBlueX.x + maximumYellowX.x)/2;
                rightBeacon.getBottomBeaconPoint().y = maximumYellowX.y;
            }
            else{
                rightBeacon.setType(BEACON_TYPE.YELLOW_BLUE);
                rightBeacon.getBottomBeaconPoint().x = (maximumBlueX.x + maximumYellowX.x)/2;
                rightBeacon.getBottomBeaconPoint().y = maximumBlueX.y;
            }
        }

        if (Math.abs(maximumRedX.x - maximumYellowX.x) < BEACON_DETERMINATION_X_THRESHOLD){
            if (maximumRedX.y < maximumYellowX.y){
                rightBeacon.setType(BEACON_TYPE.RED_YELLOW);
                rightBeacon.getBottomBeaconPoint().x = (maximumRedX.x + maximumYellowX.x)/2;
                rightBeacon.getBottomBeaconPoint().y = maximumYellowX.y;
            }
            else{
                rightBeacon.setType(BEACON_TYPE.YELLOW_RED);
                rightBeacon.getBottomBeaconPoint().x = (maximumRedX.x + maximumYellowX.x)/2;
                rightBeacon.getBottomBeaconPoint().y = maximumRedX.y;
            }
        }*/
        return rightBeacon;
    }


    private ArrayList<Pair<Point, Point>> computeBeaconCandidatesFromBatches(ArrayList<Point> batchesBlue, ArrayList<Point> batchesRed, ArrayList<Point> batchesYellow){
        ArrayList<Pair<Point, Point>> candidates = new ArrayList<>();

        ArrayList<Point> tmpBlue = new ArrayList<>();
        ArrayList<Point> tmpRed = new ArrayList<>();
        ArrayList<Point> tmpYellow = new ArrayList<>();

        tmpBlue.addAll(batchesBlue);
        tmpRed.addAll(batchesRed);
        tmpYellow.addAll(batchesYellow);


        // determine all batch candidates for beacons
        for (Point b: batchesBlue){
            for (Point r: batchesRed){
                if (Math.abs(b.x - r.x) < BEACON_DETERMINATION_X_THRESHOLD){
                    candidates.add(new Pair<Point, Point>(b, r));
                }
            }
            for (Point y: batchesYellow){
                if (Math.abs(b.x - y.x) < BEACON_DETERMINATION_X_THRESHOLD){
                    candidates.add(new Pair<Point, Point>(b, y));
                }
            }
        }

        for (Point r: batchesRed){
            for (Point y: batchesYellow){
                if (Math.abs(r.x - y.x) < BEACON_DETERMINATION_X_THRESHOLD){
                    candidates.add(new Pair<Point, Point>(r, y));
                }
            }
        }
        return candidates;
    }


    /*
    used for old determination methods
     */
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



    public boolean isRobotPositionDetermined(){
        return MY_POSITION_DETERMINED;
    }


    public RobotPosVector getRobotPosition(){
        return robotPosition;
    }




}
