package com.example.damihl.robotmove.tasks;

import android.util.Log;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.camera.SelfLocalizationManager;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.obstacleavoidance.ObstacleAvoidManager;
import com.example.damihl.robotmove.odometry.OdometryManager;
import com.example.damihl.robotmove.utils.EventCallback;
import com.example.damihl.robotmove.utils.RobotPosVector;
import com.example.damihl.robotmove.utils.TargetStack;

/*
 * Created by dAmihl on 06.04.15.
 */

/**
 * Singleton class to manage the {@link Task}s the robot has to accomplish. <br />
 * The functionality provided by this class includes: <br />
 * <hr />
 * &nbsp;&nbsp;<i>executeTask(Task)</i> - executes a given {@link Task} <br />
 * &nbsp;&nbsp;<i>executeTaskQueue(TaskQueue)</i> - executes all given {@link Task}s in a {@link com.example.damihl.robotmove.tasks.TaskQueue} <br />
 * &nbsp;&nbsp;<i>targetReachedCallback()</i> - robot stops in case it reached the target <br />
 * <hr />
 *
 *
 * @see #executeTask(Task)
 * @see #executeTaskQueue(TaskQueue)
 * @see #targetReachedCallback()
 * @see com.example.damihl.robotmove.tasks.Task
 * @see com.example.damihl.robotmove.tasks.TaskQueue
 */


public class TaskManager implements EventCallback {

    private static TaskManager instance = null;


    private enum STATE {
        OBSTACLE_FOUND,NORMAL, FINISHED, NEXT_TASK, COLOR_FOUND
    }

    private STATE CURRENT_STATE;

    private Task currentTask;
    public Task pausedTask;
    private TaskQueue taskQueue = new TaskQueue();
    private TaskThread taskThread;
    private TargetStack targetStack;
    public RobotPosVector moveToTarget;

    public static TaskManager getInstance(){
        if (instance != null) return instance;
        else{
            instance =  new TaskManager();
            return instance;
        }
    }


    private Thread taskManagerThread;


    private TaskManager(){
        taskManagerThread = new Thread(new Runnable(){

            @Override
            public void run() {
                while(true) {
                    if (CURRENT_STATE == STATE.NEXT_TASK) {
                        try {
                            if (taskThread != null)
                                taskThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        startNextTask();
                    } else if (CURRENT_STATE == STATE.OBSTACLE_FOUND){
                        try {
                            taskThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        obstacleAvoid();
                    }else if (CURRENT_STATE == STATE.COLOR_FOUND){
                        try {
                            if (taskThread != null)
                                taskThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        startNextTask();
                    } else if (CURRENT_STATE == STATE.FINISHED){
                        if (targetStack != null) {
                            if (!targetStack.isEmpty()) {
                                Log.d("TARGSTCK", "finished but target stack not empty yet.");
                                RobotPosVector newTarget = getNextTarget();
                                executeTaskQueue(Task.getNewMoveToTaskQueue(15, 15, (int) newTarget.x, (int) newTarget.y));
                            }
                        }
                    }

                    try{
                        Thread.sleep(100);
                    }catch(Exception e){

                    }
                }
            }
        });
        taskManagerThread.start();
        targetStack = new TargetStack();
    }

    public void executeTask(Task t){
        taskQueue.add(t);
        startNextTask();
    }

    public void executeTaskQueue(TaskQueue queue){

        queue.addAll(this.taskQueue);
        this.taskQueue.clear();
        this.taskQueue.addAll(queue);
        CURRENT_STATE = STATE.NEXT_TASK;
    }

    private void nextTask(){
        if (taskQueue.size() >= 1) {
            currentTask = taskQueue.remove();
        }else{
            currentTask = null;
            ControlManager.getInstance().robotStop();
        }
    }

    private void startNextTask(){
        nextTask();
        startCurrentTask();
    }

    private void startCurrentTask(){
        if (currentTask == null){
            MainActivity.getInstance().threadSafeDebugOutput("All tasks finished!");
            CURRENT_STATE = STATE.FINISHED;
            return;
        }


        CURRENT_STATE = STATE.NORMAL;
        startNewTaskThread(currentTask);

        //startStandard();
    }

    private void startStandard(){
        OdometryManager.getInstance().setTargetPosition(currentTask.getTarget());
        OdometryManager.getInstance().setEventCallback(this);
        ObstacleAvoidManager.getInstance().setEventCallback(this);
        ControlManager.getInstance().robotSetVelocity((byte) currentTask.getVelocityLeft(), (byte) currentTask.getVelocityRight());
        MainActivity.getInstance().startManagers();
    }



    private void startNewTaskThread(Task t){
        taskThread = new TaskThread(t, this);
        taskThread.start();
    }

    @Override
    public void targetReachedCallback() {
        ControlManager.getInstance().robotStop();
        //startNextTask();
    }

    @Override
    public synchronized void obstacleFoundCallback() {
        RobotPosVector oldTarget = currentTask.getTarget();
        targetStack.push(oldTarget);
        CURRENT_STATE = STATE.OBSTACLE_FOUND;
    }

    public synchronized void colorInScreenCallback(){

       if (this.CURRENT_STATE == STATE.COLOR_FOUND) return;

        Log.i("TASKMAN", "Color in screen callback.");
        this.CURRENT_STATE = STATE.COLOR_FOUND;
        executeTaskQueue(Task.getNewFindColorTaskQueue());

    }

    public synchronized void colorBroughtBack(){

        if (this.CURRENT_STATE != STATE.COLOR_FOUND) return;

        Log.i("TASKMAN", "Color in screen callback.");
        this.CURRENT_STATE = STATE.NEXT_TASK;
    }

    @Override
    public synchronized void taskFinished(){
        Log.d("TASKMAN", "Task finished");
        ControlManager.getInstance().robotStop();
        CURRENT_STATE = STATE.NEXT_TASK;
    }

    @Override
    public synchronized void taskAborted(){
        Log.d("TASKMAN", "Task aborted");
    }

    @Override
    public void positionDetermined() {
        Log.d("TASKMAN", "Position now determined! Finishing task");
        ControlManager.getInstance().robotStop();
        OdometryManager.getInstance().setCurrentPosition(SelfLocalizationManager.getInstance().getRobotPosition());
        CURRENT_STATE = STATE.NEXT_TASK;
    }

    private void obstacleAvoid(){

        MainActivity.getInstance().threadSafeDebugOutput("Obstacle now gets avoided maybe?!");
        ControlManager.getInstance().robotStop();
        ObstacleAvoidManager.getInstance().avoidObstacleBug0();
        CURRENT_STATE = STATE.NEXT_TASK;
    }

    public RobotPosVector getNextTarget(){
        RobotPosVector result;
        if (targetStack.isEmpty()) result = null;
        else result = targetStack.pop();
        return result;
    }
}

