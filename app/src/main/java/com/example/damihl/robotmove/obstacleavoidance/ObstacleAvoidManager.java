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

    private final static long sleepTime = 100;

    public ObstacleAvoidManager(ControlManager control, MainActivity application){
        this.controlManager = control;
        this.application = application;
    }

    public boolean checkObstacle(){
        String sensorData = this.controlManager.getSensorData();

        String arr[] = sensorData.split(" ");

        int valueRight = Integer.parseInt(arr[arr.length -5].substring(2), 16);
        int valueMid = Integer.parseInt(arr[arr.length -4].substring(2), 16);
        int valueLeft = Integer.parseInt(arr[arr.length -6].substring(2), 16);

        //application.threadSafeDebugOutput("Value Sensor 1 Left: "+valueLeft+" Sensor 2 Mid: "+valueMid+" Sensor 3 Right:"+valueRight);

        if(valueLeft <= 20){
            application.threadSafeDebugOutput("Obstacle Sensor Left "+valueLeft);
            return true;
        }
        if(valueMid <= 10){
            application.threadSafeDebugOutput("Obstacle Sensor Mid"+valueMid);
            return true;

        }
        if(valueRight <= 20){
            application.threadSafeDebugOutput("Obstacle Sensor Right "+valueRight);
            return true;
        }

        application.threadSafeSensorDataOutput(arr);

        return false;
    }

    public void initObstThread(final MainActivity appl, final ControlManager control){
        obstThread = new Thread(new Runnable(){

            @Override
            public void run() {
                while(control.ROBOT_MOVING) {
                    if (checkObstacle()){
                        appl.threadSafeDebugOutput("obstacle found!");
                        controlManager.robotStop();
                    }
                    try {
                        Thread.sleep(sleepTime);
                    } catch (Exception e) {
                        appl.threadSafeDebugOutput(e.toString());
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
