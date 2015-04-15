package com.example.damihl.robotmove.tasks;

import com.example.damihl.robotmove.utils.EventCallback;

/**
 * Created by dAmihl on 15.04.15.
 */
public class TaskThread extends Thread {

    private Task task;
    private EventCallback callback;

    private final static int sleepTime = 100;

    public TaskThread(Task t, EventCallback callbackObject){
        this.task = t;
        this.callback = callbackObject;
    }

    @Override
    public void run(){
        this.task.getTaskExecution().execution(task);
        while(!this.task.getTaskFinishCondition().taskFinishCondition()){
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.callback.taskFinished();
    }
}
