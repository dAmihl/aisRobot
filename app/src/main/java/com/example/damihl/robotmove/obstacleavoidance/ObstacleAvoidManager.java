package com.example.damihl.robotmove.obstacleavoidance;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;

/**
 * Created by dAmihl on 19.03.15.
 */
public class ObstacleAvoidManager {

    private ControlManager controlManager;
    private MainActivity application;

    public ObstacleAvoidManager(ControlManager control, MainActivity application){
        this.controlManager = control;
        this.application = application;
    }

    public boolean checkObstacle(){
        String sensorData = this.controlManager.getSensorData();
        /*if( insert Obstacle checking algorithm based on sensor data here)
        *   return true
        *else */
        return false;
    }


    /*
    Robot sees best at ~8cm (see Bachelor thesis)
     */
    public void driveObstacleSafe(int distance){

        int SAFE_STEP_SIZE = 1;
        int numSteps = distance / SAFE_STEP_SIZE;

        for (int i = 0; i < numSteps; i++) {
            if (checkObstacle()) {
                application.printDebugText("Obstacle detected! Robot stops!");
                controlManager.robotSetVelocity((byte) 0, (byte) 0);
            } else {
                controlManager.robotDrive((byte) SAFE_STEP_SIZE);
            }
            controlManager.pause(controlManager.getSleepTimeRobotDrive(1));
        }
    }

    public void avoidObstacleBug0(){

    }

    public void avoidObstacleBug1(){

    }

    public void avoidObstacleBug2(){

    }


}
