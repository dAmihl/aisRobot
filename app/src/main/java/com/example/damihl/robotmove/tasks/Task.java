package com.example.damihl.robotmove.tasks;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.odometry.OdometryManager;
import com.example.damihl.robotmove.utils.RobotPosVector;

import java.util.LinkedList;

/**
 * Created by dAmihl on 06.04.15.
 */
public class Task {

    private int velocityRight;
    private int velocityLeft;

    private RobotPosVector target;


    private Task(int velR, int velL, RobotPosVector target){
        this.velocityLeft = velL;
        this.velocityRight = velR;
        this.target = target;
    }

    public int getVelocityRight() {
        return velocityRight;
    }

    public int getVelocityLeft() {
        return velocityLeft;
    }

    public RobotPosVector getTarget() {
        return target;
    }



    public static Task getNewTurnTask(float byDegree){
        RobotPosVector t = OdometryManager.getInstance().getCurrentPosition().add(new RobotPosVector(0, 0, byDegree/*+9*/));
        MainActivity.getInstance().threadSafeDebugOutput("Turn Target: "+t);
        int turnSpeed = 20;
        int left;
        int right;

        if (byDegree > 0){
            left = turnSpeed;
            right = -turnSpeed;
        }else{
            left = -turnSpeed;
            right = turnSpeed;
        }
        return new Task(right, left, t);
    }

    public static Task getNewTurnToTask(float toDegree){
        float byDegree = (toDegree - OdometryManager.getInstance().getCurrentPosition().angle);
        MainActivity.getInstance().threadSafeDebugOutput("Turning. CurrDeg: "+
                OdometryManager.getInstance().getCurrentPosition().angle+"/ ToDeg: "+toDegree+" and byDeg: "+byDegree);
        return getNewTurnTask(byDegree);
    }

    public static Task createNewTask(int velR, int velL, RobotPosVector t){
        return new Task(velR, velL, t);
    }

    public static TaskQueue getNewMoveToTaskQueue(int velR, int velL, int x, int y){
        TaskQueue queue = new TaskQueue();
        RobotPosVector moveTarget = new RobotPosVector(x, y, 0);
        float angle = OdometryManager.getInstance().getCurrentPosition().angleBetween(moveTarget);
        Task turnTask = getNewTurnToTask(angle);
        MainActivity.getInstance().threadSafeDebugOutput("TurnTaskTarget: "+turnTask.getTarget());
        queue.add(turnTask);

        Task moveTask = getNewMoveTask(velR, velL, x, y);

        queue.add(moveTask);
        MainActivity.getInstance().threadSafeDebugOutput("MoveTaskTarget: "+moveTask.getTarget());

        return queue;
    }

    public static Task getNewMoveTask(int velR, int velL, int x, int y){
        RobotPosVector target = new RobotPosVector(x, y, OdometryManager.getInstance().getCurrentPosition().angle);
        return createNewTask(velR, velL, target);
    }

    public static TaskQueue getNewMoveByLeftTaskQueue(int velR, int velL, int moveBy){

        float turnAngle = -90;
        return getNewMoveByTaskQueue(velR, velL, moveBy, turnAngle);
    }

    public static TaskQueue getNewMoveByRightTaskQueue(int velR, int velL, int moveBy){

        float turnAngle = 90;
        return getNewMoveByTaskQueue(velR, velL, moveBy, turnAngle);

    }

    private static TaskQueue getNewMoveByTaskQueue(int velR, int velL, int moveBy, float angle){

        TaskQueue qu = new TaskQueue();
        qu.add(Task.getNewTurnTask(angle));
        float currentAngle = OdometryManager.getInstance().getCurrentPosition().angle;

        RobotPosVector move = new RobotPosVector((float)(moveBy * Math.cos(Math.toRadians(currentAngle + angle))),
                (float)(moveBy * Math.sin(Math.toRadians(currentAngle + angle))),
                currentAngle);

        RobotPosVector target = OdometryManager.getInstance().getCurrentPosition().add(move);

        qu.addAll(getNewMoveToTaskQueue(velR, velL, (int) target.x, (int) target.y));
        return qu;
    }
}
