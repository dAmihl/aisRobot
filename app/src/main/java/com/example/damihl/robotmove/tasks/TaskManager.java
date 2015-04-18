package com.example.damihl.robotmove.tasks;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.obstacleavoidance.ObstacleAvoidManager;
import com.example.damihl.robotmove.odometry.OdometryManager;
import com.example.damihl.robotmove.utils.EventCallback;
import com.example.damihl.robotmove.utils.RobotPosVector;

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

    private Task currentTask;
    private TaskQueue taskQueue = new TaskQueue();
    private TaskThread taskThread;

    public static TaskManager getInstance(){
        if (instance != null) return instance;
        else{
            instance =  new TaskManager();
            return instance;
        }
    }

    private TaskManager(){

    }

    public void executeTask(Task t){
        taskQueue.add(t);
        startNextTask();
    }

    public void executeTaskQueue(TaskQueue queue){
        this.taskQueue.clear();
        this.taskQueue.addAll(queue);
        this.startNextTask();

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
            return;
        }
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
        //ControlManager.getInstance().robotStop();
        //startNextTask();
    }

    @Override
    public void obstacleFoundCallback() {
            RobotPosVector oldTarget = OdometryManager.getInstance().getTargetPosition();
            MainActivity.getInstance().threadSafeDebugOutput("Obstacle now gets avoided maybe?!");
            ControlManager.getInstance().robotStop();
            ObstacleAvoidManager.getInstance().avoidObstacleBug0(oldTarget);
    }

    @Override
    public void taskFinished(){
        ControlManager.getInstance().robotStop();
        startNextTask();
    }
}
