package com.example.damihl.robotmove.utils;

/**
 * Created by dAmihl on 01.04.15.
 */
public class RobotPosVector {


    private static final int STANDARD_POSITION_OFFSET = 5;
    private static final int STANDARD_ANGLE_OFFSET = 2;
    private static final int CALIBRATION_ANGLE_OFFSET = 0;

    public float x;
    public float y;
    private float angle;

    public RobotPosVector(float x, float y, float theta){
        this.x = x;
        this.y = y;
        this.angle = theta;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getAngle(){
        return angle;
    }

    public void addAngle(float addTo){
        this.angle += addTo;
        this.angle = this.angle % 360;
    }

    public RobotPosVector add(RobotPosVector other){
        this.x += other.x;
        this.y += other.y;
        this.angle += other.angle;
        return this;
    }

    public RobotPosVector addXY(RobotPosVector other){
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public float angleBetween(RobotPosVector other){
        return (float) Math.toDegrees(Math.atan2(other.x - x, other.y - y));
    }

    public boolean equalsXY(RobotPosVector other){
        return x == other.x && y == other.y;
    }

    public boolean equals(RobotPosVector other){
        return equalsXY(other) && angle == other.angle;
    }

    public boolean isAt(RobotPosVector other){
        int offset = STANDARD_POSITION_OFFSET;
        return isAt(other, offset);
    }

    public boolean isAt(RobotPosVector other, int offset){
        return (x <= (other.x + offset)) && (x >= (other.x - offset))
                && (y <= (other.y + offset)) && (y >= (other.y - offset));
    }

    public boolean isAtWithAngle(RobotPosVector other){
        int offset = STANDARD_POSITION_OFFSET;
        int angleOffset = STANDARD_ANGLE_OFFSET;
        return isAtWithAngle(other, offset, angleOffset);
    }

    public boolean isAtWithAngle(RobotPosVector other, int offset, int angleOffset){
        return      (x <= (other.x + offset)) && (x >= (other.x - offset))
                &&  (y <= (other.y + offset)) && (y >= (other.y - offset))
                &&  (angle <= (other.angle + CALIBRATION_ANGLE_OFFSET + angleOffset)) && (angle >= (other.angle + CALIBRATION_ANGLE_OFFSET - angleOffset));
    }

    public String toString(){
        return "("+x+"/"+y+"/"+angle+")";
    }

    public double dist2goal(RobotPosVector other){
        return Math.sqrt((other.x - this.x)*(other.x - this.x)+(other.y - this.y)*(other.y - this.y));
    }

    public float angle2goal(RobotPosVector other){
        double alpha = 0;
       // alpha = ((Math.atan2 ((double)(other.y - this.y), ((double)(other.x - this.x))) * 180/Math.PI));
        alpha = ((Math.atan2 ((double)(other.y - this.y), ((double)(other.x - this.x)))));

        alpha = Math.toDegrees(alpha);
        alpha = alpha % 360;
        return (float) alpha;
        /*
        double theta_wrap = this.angle*180/Math.PI;
        if(theta_wrap > 180){
            theta_wrap = theta_wrap - 360;
        }else if(theta_wrap < - 180){
            theta_wrap = theta_wrap + 360;
        }
        double angle2goal = alpha - theta_wrap;

        return (float)angle2goal;*/
    }

    public double distance2mline(RobotPosVector goal){
        double distance = this.x * goal.y - this.y * goal.x;
        if(distance > 0) return ((this.x * goal.y - this.y * goal.x)/(Math.sqrt(goal.y * goal.y + goal.x * goal.x)));
        else return (-(this.x * goal.y - this.y * goal.x)/(Math.sqrt(goal.y * goal.y + goal.x * goal.x)));
    }



}
