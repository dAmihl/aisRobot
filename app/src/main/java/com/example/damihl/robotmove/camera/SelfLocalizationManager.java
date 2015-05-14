package com.example.damihl.robotmove.camera;

import com.example.damihl.robotmove.utils.RobotPosVector;
import com.example.damihl.robotmove.utils.WorldPoint;

import org.opencv.core.Point;

/**
 * Created by dAmihl on 14.05.15.
 */
public class SelfLocalizationManager {

    // names from upper to lower color
    private static final WorldPoint BEACON_BLUE_YELLOW_POSITION = new WorldPoint(-125, 125);
    private static final WorldPoint BEACON_BLUE_RED_POSITION = new WorldPoint(0, 125);
    private static final WorldPoint BEACON_RED_YELLOW_POSITION = new WorldPoint(125, 125);

    private static final WorldPoint BEACON_WHITE_BLUE_POSITION = new WorldPoint(-125, 0);
    private static final WorldPoint BEACON_WHITE_RED_POSITION = new WorldPoint(125, 0);

    private static final WorldPoint BEACON_YELLOW_BLUE_POSITION = new WorldPoint(-125, -125);
    private static final WorldPoint BEACON_RED_BLUE_POSITION = new WorldPoint(0, -125);
    private static final WorldPoint BEACON_YELLOW_RED_POSITION = new WorldPoint(125, -125);








    public RobotPosVector computeMyLocation(){
        RobotPosVector myPos = new RobotPosVector(0,0,0);

        int numContoursBlue = CameraManager.getInstance().countNumberOfContours(CameraManager.getInstance().BLUE_COLOR);
        int numContoursRed = CameraManager.getInstance().countNumberOfContours(CameraManager.getInstance().RED_COLOR);
        int numContoursYellow = CameraManager.getInstance().countNumberOfContours(CameraManager.getInstance().YELLOW_COLOR);




        // compute the stuff
        return myPos;
    }




}
