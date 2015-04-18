package com.example.damihl.robotmove.obstacleavoidance;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.sensors.SensorManager;
import com.example.damihl.robotmove.tasks.Task;
import com.example.damihl.robotmove.tasks.TaskManager;
import com.example.damihl.robotmove.tasks.TaskQueue;
import com.example.damihl.robotmove.utils.EventCallback;
import com.example.damihl.robotmove.utils.RobotPosVector;

/**
 * <br />
 * Created by dAmihl on 19.03.15.
 * <br />
 * <br />
 * <b>
 * ObstacleAvoidManager:
 * </b>
 * <br />
 * This singleton class makes sure that the robot won't collide with an obstacle<br />
 * The functionality is implemented in an own thread, which checks continuously for obstacles
 * in a method called {@link com.example.damihl.robotmove.obstacleavoidance.ObstacleAvoidManager#checkObstacle()}. <br />
 * If this method finds an obstacle it calls the {@link com.example.damihl.robotmove.tasks.TaskManager#obstacleFoundCallback()}
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

    public void joinThread() throws InterruptedException {
        if (this.obstThread != null)
            this.obstThread.join();
    }


    /**
     * checks for obstacles
     * @return
     *          <b>true</b> - if the value of the <i>left/right sensor</i> get below 20 <br />
     *          <b>true</b> - if the value of the <i>center sensor</i> get below 10 <br />
     *          <b>false</b> - otherwise
     */
    public boolean checkObstacle(){


        int valueLeft = SensorManager.getInstance().getLeftSensorData();
        int valueRight = SensorManager.getInstance().getRightSensorData();
        int valueMid = SensorManager.getInstance().getMidSensorData();

        //application.threadSafeDebugOutput("Value Sensor 1 Left: "+valueLeft+" Sensor 2 Mid: "+valueMid+" Sensor 3 Right:"+valueRight);

        if(valueLeft <= 20 && valueLeft > 1){
            application.threadSafeDebugOutput("Obstacle Sensor Left "+valueLeft);
            return true;
            //return false;
        }
        if(valueMid <= 10 && valueMid > 1){
            application.threadSafeDebugOutput("Obstacle Sensor Mid"+valueMid);
            return true;

        }
        if(valueRight <= 20 && valueMid > 1){
            application.threadSafeDebugOutput("Obstacle Sensor Right "+valueRight);
            return true;
        }


        return false;
    }

    public void initObstThread(final MainActivity appl, final ControlManager control){
        obstThread = new Thread(new Runnable(){

            @Override
            public void run() {
                while(control.ROBOT_MOVING) {
                    if (checkObstacle()){
                        //obstacleFoundEventCallback.obstacleFoundCallback();
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
           // while (obstThread.isAlive());
           // initObstThread(appl, control);
            obstThread.start();
        }catch(Exception e){
            appl.threadSafeDebugOutput("obstacle avoid error: "+e);
        }
        appl.threadSafeDebugOutput("obstacle avoid started");
    }



    /*

    Robot sees best at ~8cm (see Bachelor thesis)
     */
    @Deprecated
    public void driveObstacleSafe(int distance){

        int SAFE_STEP_SIZE = 1;
        int numSteps = distance / SAFE_STEP_SIZE;

        for (int i = 0; i < numSteps; i++) {
            if (checkObstacle()) {
                application.threadSafeDebugOutput("Obstacle detected! Robot stops!");
                controlManager.robotSetVelocity((byte) 0, (byte) 0);
            } else {
                controlManager.robotDrive((byte) SAFE_STEP_SIZE);
                controlManager.pause(controlManager.getSleepTimeRobotDrive(1));
            }
        }
    }

    public void avoidObstacleBug0(RobotPosVector toTarget){
        MainActivity.getInstance().threadSafeDebugOutput("Bug0 starting");
        TaskQueue queue = new TaskQueue();
       // queue.add(Task.getNewTurnTask(90));
        //queue.addAll(Task.getNewMoveByRightTaskQueue(20, 20, 1000));


        int x = (int) toTarget.x;
        int y = (int) toTarget.y;
       // queue.addAll(Task.getNewMoveToTaskQueue(20, 20, x,y));
        TaskManager.getInstance().executeTaskQueue(queue);
    }

    public void avoidObstacleBug1(){

    }

    public void avoidObstacleBug2(){

    }

    public void Bug2impl(RobotPosVector goal){
        int state = 0;

        // state 0 no obstacle ahead
        if(state == 0){
            if(checkObstacle()){
               //obstacle ahead state 1
               state = 1;
            }
        }else{
            state = 0;

        }
    }

    public void setEventCallback(EventCallback called) {
        this.obstacleFoundEventCallback = called;
    }
}
