package com.example.damihl.robotmove.utils;

/**
 * Created by dAmihl on 14.05.15.
 */
public class WorldPoint {

    private float x;
    private float y;

    public WorldPoint(){
        this.x = 0;
        this.y = 0;
    }

    public WorldPoint(float x, float y){
        this.x = x;
        this.y = y;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    @Override
    public String toString(){
        return "("+x+"/"+y+")";
    }

    public void switchOrientation(){
        float tmp = this.x;
        this.x = this.y;
        this.y = tmp;
    }

    public static double angleBetween(WorldPoint p1, WorldPoint p2){
        double d = (p1.getX() * p2.getX()) + (p1.getY() * p2.getY());
        d = d / (WorldPoint.length(p1) * WorldPoint.length(p2));
        double angle = Math.acos(d);
        angle = Math.toDegrees(angle);
        return angle;
    }

    private static double length(WorldPoint p){
        double length = Math.sqrt((p.getX() * p.getX()) + (p.getY() * p.getY()));
        return length;
    }
}
