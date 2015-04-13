package com.example.damihl.robotmove.paths;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.obstacleavoidance.ObstacleAvoidManager;
import com.example.damihl.robotmove.tasks.Task;
import com.example.damihl.robotmove.tasks.TaskQueue;

import java.util.LinkedList;

/**
 * Created by dAmihl on 13.03.15.
 */
public class PathDriveManager {

    private ControlManager controlManager;
    private MainActivity activity;
    private ObstacleAvoidManager obstacleAvoidance;

    private static PathDriveManager instance = null;

    public static PathDriveManager getInstance(){
        if (instance != null) return instance;
        else {
            instance = new PathDriveManager();
            return instance;
        }
    }

    private PathDriveManager(){
        this.controlManager = ControlManager.getInstance();
        this.activity = MainActivity.getInstance();
        this.obstacleAvoidance = ObstacleAvoidManager.getInstance();
    }

    public void driveSquare(int squareSize){
        if (!activity.checkConnection()) return;

        int waitTimeMove = 1000 * squareSize / 20;
        int waitTimeTurn = 1000;
        int turnsize = 99;

        controlManager.robotDrive((byte) squareSize);
        controlManager.pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);
        controlManager.pause(waitTimeTurn);
        controlManager.robotDrive((byte) squareSize);
        controlManager.pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);
        controlManager.pause(waitTimeTurn);
        controlManager.robotDrive((byte) squareSize);
        controlManager.pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);
        controlManager.pause(waitTimeTurn);
        controlManager.robotDrive((byte) squareSize);
        controlManager.pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);

    }


    public TaskQueue getSquareTestPath(int size){
        TaskQueue queue = new TaskQueue();
        int speedR = 15;
        int speedL = 15;

        queue.add(Task.getNewMoveTask(speedR, speedL, size, 0));
        queue.add(Task.getNewTurnTask(90));
        queue.add(Task.getNewMoveTask(speedR, speedL, size, size));
        queue.add(Task.getNewTurnTask(90));
        queue.add(Task.getNewMoveTask(speedR, speedL, 0, size));
        queue.add(Task.getNewTurnTask(90));
        queue.add(Task.getNewMoveTask(speedR, speedL, 0, 0));

        return queue;
    }


}
