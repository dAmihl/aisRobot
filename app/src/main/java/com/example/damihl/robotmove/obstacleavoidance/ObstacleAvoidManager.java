package com.example.damihl.robotmove.obstacleavoidance;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.tasks.Task;
import com.example.damihl.robotmove.tasks.TaskManager;
import com.example.damihl.robotmove.tasks.TaskQueue;
import com.example.damihl.robotmove.utils.EventCallback;
import com.example.damihl.robotmove.utils.RobotPosVector;

/**
 * Created by dAmihl on 19.03.15.
 */
public class ObstacleAvoidManager {

    private static ObstacleAvoidManager instance = null;

    public static ObstacleAvoidManager getInstance(){
        if (instance != null) return instance;
        else{
            instance = new ObstacleAvoidManager();
            return instance;
        }
    }


    private EventCallback obstacleFoundEventCallback = null;

    private ControlManager controlManager;
    private MainActivity application;
    private Thread obstThread;

    private final static long sleepTime = 100;

    private ObstacleAvoidManager(){
        this.controlManager = ControlManager.getInstance();
        this.application = MainActivity.getInstance();
        this.obstacleFoundEventCallback = TaskManager.getInstance();
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
                        obstacleFoundEventCallback.obstacleFoundCallback();
                        appl.threadSafeDebugOutput("obstacle found!");
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
            appl.printDebugText("obstacle avoid error: "+e);
        }
        appl.printDebugText("obstacle avoid started");
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

    public void avoidObstacleBug0(RobotPosVector toTarget){
        TaskQueue queue = Task.getNewMoveByRightTaskQueue(20, 20, 1000);
        int x = (int) toTarget.x;
        int y = (int) toTarget.y;
        queue.addAll(Task.getNewMoveToTaskQueue(20, 20, x,y));
        TaskManager.getInstance().executeTaskQueue(queue);
    }

    public void avoidObstacleBug1(){

    }

    public void avoidObstacleBug2(){

    }


    public void setEventCallback(EventCallback called) {
        this.obstacleFoundEventCallback = called;
    }
}
