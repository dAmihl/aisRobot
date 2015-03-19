package com.example.damihl.robotmove.paths;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.obstacleavoidance.ObstacleAvoidManager;

/**
 * Created by dAmihl on 13.03.15.
 */
public class PathDriveManager {

    private ControlManager controlManager;
    private MainActivity activity;
    private ObstacleAvoidManager obstacleAvoidance;

    public PathDriveManager(ControlManager con, MainActivity act, ObstacleAvoidManager obstacleManager){
        this.controlManager = con;
        this.activity = act;
        this.obstacleAvoidance = obstacleManager;
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

    public void driveSquareObstacleSafe(int squareSize){
        if (!activity.checkConnection()) return;

        int waitTimeMove = controlManager.getSleepTimeRobotDrive(squareSize);
        int waitTimeTurn = controlManager.getSleepTimeRobotTurn(90);
        int turnsize = controlManager.computeCorrectDegree(90);//99

        obstacleAvoidance.driveObstacleSafe((byte) squareSize);
        controlManager.pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);
        controlManager.pause(waitTimeTurn);
        obstacleAvoidance.driveObstacleSafe((byte) squareSize);
        controlManager.pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);
        controlManager.pause(waitTimeTurn);
        obstacleAvoidance.driveObstacleSafe((byte) squareSize);
        controlManager.pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);
        controlManager.pause(waitTimeTurn);
        obstacleAvoidance.driveObstacleSafe((byte) squareSize);
        controlManager.pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);

    }

}
