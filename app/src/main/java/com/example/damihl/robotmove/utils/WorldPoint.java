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

}
