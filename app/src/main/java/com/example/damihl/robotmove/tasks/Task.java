package com.example.damihl.robotmove.tasks;

import android.util.Log;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.camera.CameraManager;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.obstacleavoidance.ObstacleAvoidManager;
import com.example.damihl.robotmove.odometry.OdometryManager;
import com.example.damihl.robotmove.utils.RobotPosVector;

import java.util.LinkedList;

/**
 * <br />
 * Created by dAmihl on 06.04.15.
 * <br />
 * <br />
 * <b>
 *     Task:
 * </b>
 * <br />
 * a task the robot has to accomplish (i.e move to a specified target) <br />
 */
public class Task {

    private int velocityRight;
    private int velocityLeft;

    private TaskExecution execution;
    private TaskCondition finishCondition;


    private RobotPosVector target;


    private Task(int velR, int velL, RobotPosVector target, TaskExecution exec, TaskCondition condition){
        this.velocityLeft = velL;
        this.velocityRight = velR;
        this.target = target;
        this.execution = exec;
        this.finishCondition = condition;
    }

    public int getVelocityRight() {
        return velocityRight;
    }

    public int getVelocityLeft() {
        return velocityLeft;
    }

    public RobotPosVector getTarget() {
        return target;
    }

    public TaskExecution getTaskExecution(){
        return execution;
    }

    public TaskCondition getTaskFinishCondition(){
        return finishCondition;
    }



    /*
    STANDARD MOVEMENT TASKS
     */
    public static Task getNewTurnTask(float byDegree){
        //RobotPosVector t = OdometryManager.getInstance().getCurrentPosition().add(new RobotPosVector(0, 0, byDegree));
        RobotPosVector t = new RobotPosVector(OdometryManager.getInstance().getCurrentPosition().x + 0,
                OdometryManager.getInstance().getCurrentPosition().y + 0,
                OdometryManager.getInstance().getCurrentPosition().getAngle() + byDegree);
        MainActivity.getInstance().threadSafeDebugOutput("Turn Target: "+t);
        int turnSpeed = 20;
        int left;
        int right;

        if (byDegree > 0){
            left = turnSpeed;
            right = -turnSpeed;
        }else{
            left = -turnSpeed;
            right = turnSpeed;
        }

        TaskCondition cond = new TaskCondition() {

            @Override
            public boolean taskFinishCondition() {
                return OdometryManager.getInstance().checkTargetReached();
            }
        };

        return new Task(right, left, t, getStandardMoveTaskExecution(), cond);
    }

    public static Task getNewTurnToTask(int velR, int velL, int x, int y){
        //float byDegree = (toDegree - OdometryManager.getInstance().getCurrentPosition().getAngle());
        /*MainActivity.getInstance().threadSafeDebugOutput("Turning. CurrDeg: "+
                OdometryManager.getInstance().getCurrentPosition().getAngle()+"/ ToDeg: "+toDegree+" and byDeg: "+byDegree);*/

        RobotPosVector t = new RobotPosVector(x, y, 0);
        return new Task(velR, velL, t, getStandardTurnTaskExecution(), getStandardTurnTaskCondition());
    }

    public static Task createNewTurnTask(int velR, int velL, RobotPosVector t){
        return new Task(velR, velL, t, getStandardTurnTaskExecution(),getStandardTurnTaskCondition());
    }

    public static Task createNewMoveTask(int velR, int velL, RobotPosVector t){
        return new Task(velR, velL, t, getStandardMoveTaskExecution(),getStandardTaskCondition());
    }

    public static TaskQueue getNewMoveToTaskQueue(int velR, int velL, int x, int y){
        TaskQueue queue = new TaskQueue();

        Task turnTask = getNewTurnToTask(velR, velL, x, y);
        MainActivity.getInstance().threadSafeDebugOutput("TurnTaskTarget: " + turnTask.getTarget());
        queue.add(turnTask);


        Task moveTask = getNewMoveTask(velR, velL, x, y);

        queue.add(moveTask);
        MainActivity.getInstance().threadSafeDebugOutput("MoveTaskTarget: "+moveTask.getTarget());

        return queue;
    }

    public static Task getNewMoveTask(int velR, int velL, int x, int y){
        RobotPosVector target = new RobotPosVector(x, y, OdometryManager.getInstance().getCurrentPosition().getAngle());
        return createNewMoveTask(velR, velL, target);
    }

    public static TaskQueue getNewMoveByLeftTaskQueue(int velR, int velL, int moveBy){

        float turnAngle = -90;
        return getNewMoveByTaskQueue(velR, velL, moveBy, turnAngle);
    }

    public static TaskQueue getNewMoveByRightTaskQueue(int velR, int velL, int moveBy){

        float turnAngle = 90;
        return getNewMoveByTaskQueue(velR, velL, moveBy, turnAngle);

    }

    private static TaskQueue getNewMoveByTaskQueue(int velR, int velL, int moveBy, float angle){

        TaskQueue qu = new TaskQueue();
        //qu.add(Task.getNewTurnToTask(velR, velL, (int) (moveBy * Math.cos(Math.toRadians(angle))), (int) (moveBy * Math.sin(Math.toRadians(angle)))));
        float currentAngle = OdometryManager.getInstance().getCurrentPosition().getAngle();
        float effAngle = currentAngle + angle;
        effAngle = effAngle % 360;

        RobotPosVector move = new RobotPosVector((float)(moveBy * Math.cos(Math.toRadians(effAngle))),
                (float)(moveBy * Math.sin(Math.toRadians(effAngle))),
                effAngle);

        RobotPosVector target = move.add(OdometryManager.getInstance().getCurrentPosition());

        qu.addAll(getNewMoveToTaskQueue(velR, velL, (int) target.x, (int) target.y));
        return qu;
    }


    public static Task getPrimitiveTaskTurn(final int degree){
        TaskExecution exec = new TaskExecution() {
            @Override
            public void execution(Task t) {
                ControlManager.getInstance().robotTurn((byte) degree);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                OdometryManager.getInstance().getCurrentPosition().add(new RobotPosVector(0, 0, degree));
            }
        };

        TaskCondition condition = new TaskCondition() {
            @Override
            public boolean taskFinishCondition() {
                return true;
            }
        };

        return new Task(0, 0, OdometryManager.getInstance().getCurrentPosition(), exec, condition);
    }

    public static TaskQueue getNewFindColorTaskQueue(){
        TaskQueue queue = new TaskQueue();
        queue.add(getNewTurnForColorTask());
        queue.add(getNewMoveUntilColorTask());
        return queue;
    }


    public static Task getNewTurnForColorTask(){
        return new Task(12,-12,OdometryManager.getInstance().getCurrentPosition(),getStandardMoveTaskExecution(), getColorSearchTaskCondition());
    }

    public static Task getNewMoveUntilColorTask(){
        return new Task(12,-12,OdometryManager.getInstance().getCurrentPosition(),getStandardMoveTaskExecution(), getColorInRangeTaskCondition());
    }



    /*
    STANDARD TASK CONDITION AND EXECUTIONS
    */
    public static TaskCondition getStandardTaskCondition(){
        return new TaskCondition() {
            @Override
            public boolean taskFinishCondition() {
                if (ObstacleAvoidManager.getInstance().checkObstacle()){
                    TaskManager.getInstance().obstacleFoundCallback();
                    return true;
                }
                if (OdometryManager.getInstance().getCurrentPosition().isAt(OdometryManager.getInstance().getTargetPosition())){
                    TaskManager.getInstance().targetReachedCallback();
                    return true;
                }
                return false;
            }
        };
    }

    public static TaskCondition getColorSearchTaskCondition(){
        return new TaskCondition() {
            @Override
            public boolean taskFinishCondition() {
                return CameraManager.getInstance().checkColorInMiddle();
            }
        };
    }

    public static TaskCondition getColorInRangeTaskCondition(){
        return new TaskCondition() {
            @Override
            public boolean taskFinishCondition() {
                return CameraManager.getInstance().checkColorInRange();
            }
        };
    }


    public static TaskCondition getStandardTurnTaskCondition(){
        return new TaskCondition() {

            @Override
            public boolean taskFinishCondition() {
                if (OdometryManager.getInstance().checkTargetReached()){
                    TaskManager.getInstance().targetReachedCallback();
                    return true;
                }
                return false;
            }
        };
    }

    public static TaskExecution getStandardMoveTaskExecution(){
        return new TaskExecution() {
            @Override
            public void execution(Task t) {
                Log.d("MOVETASK", "movetask to "+t.getTarget());
                OdometryManager.getInstance().setTargetPosition(t.getTarget());
                OdometryManager.getInstance().setEventCallback(TaskManager.getInstance());
                ObstacleAvoidManager.getInstance().setEventCallback(TaskManager.getInstance());
                MainActivity.getInstance().joinManagerThreads();
                ControlManager.getInstance().robotSetVelocity((byte) t.getVelocityLeft(), (byte) t.getVelocityRight());
                MainActivity.getInstance().startManagers();
            }
        };
    }

    public static TaskExecution getStandardTurnTaskExecution(){
        return new TaskExecution() {
            @Override
            public void execution(Task t) {

                RobotPosVector moveTarget = t.getTarget();
                float angle = OdometryManager.getInstance().getCurrentPosition().angle2goal((moveTarget));
                Log.d("TURNTASK", "angle to goal: "+angle);
                RobotPosVector effTarget = new RobotPosVector(OdometryManager.getInstance().getCurrentPosition().x,
                        OdometryManager.getInstance().getCurrentPosition().y, angle);

                t.target = effTarget;

              //  int turnSpeed = Math.min(Math.abs(t.getVelocityLeft()), Math.abs(t.getVelocityRight()));
                int turnSpeed = 12;
                int left = turnSpeed;
                int right = -turnSpeed;

                if (angle > 0){
                    left =  turnSpeed;
                    right = -turnSpeed;
                }else{
                   left = -turnSpeed;
                   right = turnSpeed;
                   // left =  turnSpeed;
                   // right = -turnSpeed;

                    t.target.addAngle(360);
                }

                t.velocityLeft = left;
                t.velocityRight = right;

                if (Math.abs(angle) < 5) {
                    Log.d("TURNTASK", "Angle to low. task finished");
                    TaskManager.getInstance().taskFinished();
                    return;
                }

                OdometryManager.getInstance().setTargetPosition(t.getTarget());
                OdometryManager.getInstance().setEventCallback(TaskManager.getInstance());
                ObstacleAvoidManager.getInstance().setEventCallback(TaskManager.getInstance());
                MainActivity.getInstance().joinManagerThreads();
                ControlManager.getInstance().robotSetVelocity((byte) t.getVelocityLeft(), (byte) t.getVelocityRight());
                MainActivity.getInstance().startManagers();
            }
        };
    }
}
