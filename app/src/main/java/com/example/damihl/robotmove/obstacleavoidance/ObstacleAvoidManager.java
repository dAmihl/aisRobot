package com.example.damihl.robotmove.obstacleavoidance;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;

/**
 * Created by dAmihl on 19.03.15.
 */
public class ObstacleAvoidManager {

    private ControlManager controlManager;
    private MainActivity application;
    private Thread obstThread;

    public ObstacleAvoidManager(ControlManager control, MainActivity application){
        this.controlManager = control;
        this.application = application;
    }

    public boolean checkObstacle(){
        String sensorData = this.controlManager.getSensorData();

        String arr[] = sensorData.split(" ");

        Long value = Long.parseLong(arr[arr.length-2].substring(2), 16);
        application.threadSafeDebugOutput("Long value sensor: "+value);


        if(arr[arr.length -2].equals("0x1d") || arr[arr.length -2].equals("0x1c") || arr[arr.length -2].equals("0x1e"))
        {
            return true;
        }
        application.threadSafeDebugOutput("SensorData: "+sensorData);
        /*if( insert Obstacle checking algorithm based on sensor data here)
        *   return true
        *else */
        return false;
    }

    public void initObstThread(final MainActivity appl, final ControlManager control){
        obstThread = new Thread(new Runnable(){

            @Override
            public void run() {
                int i = 0;
                while(control.ROBOT_MOVING) {
                    if (checkObstacle()){
                        appl.threadSafeDebugOutput("obstacle found!");
                    }
                }
            }

        });
    }

    public void startObstacleDetection(final ControlManager control, final MainActivity appl){
        try {
            initObstThread(appl, control);
            obstThread.start();
        }catch(Exception e){
            appl.printDebugText("odometry error: "+e);
        }
        appl.printDebugText("odometry started");
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
                controlManager.pause(controlManager.getSleepTimeRobotDrive(1));
            }
        }
    }

    public void avoidObstacleBug0(){

    }

    public void avoidObstacleBug1(){

    }

    public void avoidObstacleBug2(){

    }


}
