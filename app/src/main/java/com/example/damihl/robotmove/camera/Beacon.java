package com.example.damihl.robotmove.camera;

import org.opencv.core.Point;

/**
 * Created by dAmihl on 29.05.15.
 */
public class Beacon {

    private SelfLocalizationManager.BEACON_TYPE type;
    private Point bottomBeaconPoint;

    public Beacon(SelfLocalizationManager.BEACON_TYPE type, Point bottomPoint) {
        this.type = type;
        this.bottomBeaconPoint = bottomPoint;
    }

    public Beacon(SelfLocalizationManager.BEACON_TYPE type) {
        this.type = type;
        this.bottomBeaconPoint = new Point();
    }

    public SelfLocalizationManager.BEACON_TYPE getType() {
        return type;
    }

    public void setType(SelfLocalizationManager.BEACON_TYPE type) {
        this.type = type;
    }

    public Point getBottomBeaconPoint() {
        return bottomBeaconPoint;
    }

    public void setBottomBeaconPoint(Point bottomBeaconPoint) {
        this.bottomBeaconPoint = bottomBeaconPoint;
    }
}
