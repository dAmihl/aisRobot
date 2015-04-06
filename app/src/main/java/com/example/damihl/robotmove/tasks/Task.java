package com.example.damihl.robotmove.tasks;

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
        RobotPosVector t = OdometryManager.getInstance().getCurrentPosition().add(new RobotPosVector(0, 0, byDegree));
        return new Task(10, -10, t);
    }

    public static Task getNewTurnToTask(float toDegree){
        float byDegree = (OdometryManager.getInstance().getCurrentPosition().angle - toDegree);
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
        queue.add(turnTask);

        Task moveTask = getNewMoveTask(velR, velL, x, y);

        queue.add(moveTask);

        return queue;
    }

    public static Task getNewMoveTask(int velR, int velL, int x, int y){
        RobotPosVector target = new RobotPosVector(x, y, 0);
        return createNewTask(velR, velL, target);
    }
}
