package com.example.damihl.robotmove.tasks;

import android.util.Log;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.camera.CameraManager;
import com.example.damihl.robotmove.camera.SelfLocalizationManager;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.obstacleavoidance.ObstacleAvoidManager;
import com.example.damihl.robotmove.odometry.OdometryManager;
import com.example.damihl.robotmove.utils.RobotPosVector;
import com.example.damihl.robotmove.utils.WorldPoint;

import org.opencv.core.Point;

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

    public final static int STANDARD_MOVE_SPEED = 25;
    public final static int STANDARD_TURN_SPEED = 12;

    private int velocityRight;
    private int velocityLeft;

    private TaskExecution execution;
    private TaskCondition finishCondition;

    private static int COLLECTING_BALL_TARGET_X = 0;
    private static int COLLECTING_BALL_TARGET_Y = 0;


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


    public void setFinishCondition(TaskCondition fin){
        this.finishCondition = fin;
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

    public static Task getNewColorAwareTurnToTask(int velR, int velL, int x, int y){
       Task t = getNewTurnToTask(velR, velL, x, y);
        t.setFinishCondition(getStandardColorAwareTurnTaskCondition());
        return t;
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
        queue.add(turnTask);


        Task moveTask = getNewMoveTask(velR, velL, x, y);

        queue.add(moveTask);

        return queue;
    }

    public static TaskQueue getNewMoveToWithoutObstacleAvoidTaskQueue(int velR, int velL, int x, int y){
        TaskQueue queue = new TaskQueue();

        Task turnTask = getNewTurnToTask(velR, velL, x, y);
        queue.add(turnTask);


        Task moveTask = getNewMoveWithoutObstacleAvoidTask(velR, velL, x, y);


        queue.add(moveTask);

        return queue;
    }


    public static Task getNewMoveWithoutObstacleAvoidTask(int velR, int velL, int x, int y){
        RobotPosVector target = new RobotPosVector(x, y, OdometryManager.getInstance().getCurrentPosition().getAngle());
        return new Task(velR, velL, target, getStandardMoveTaskExecution(),getMoveWithoutObstacleAvoidTaskCondition());
    }


    // turns slowly until position is determined
    public static TaskQueue getSelfLocalizationTaskQueue(){

        TaskQueue queue = new TaskQueue();

        TaskCondition cond = new TaskCondition() {
            @Override
            public boolean taskFinishCondition() {
                CameraManager.getInstance().setBeaconDetectionOn(true);
                if (SelfLocalizationManager.getInstance().isRobotPositionDetermined()){
                    TaskManager.getInstance().positionDetermined();
                    return true;
                };
                return false;
            }
        };

        TaskExecution exec = new TaskExecution() {
            @Override
            public void execution(Task t) {
                ControlManager.getInstance().robotSetVelocity((byte) t.getVelocityLeft(), (byte) t.getVelocityRight());
            }
        };

        queue.add(new Task(5,-5,new RobotPosVector(0,0,0),exec,cond));
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
        int targetPosX = (int) (COLLECTING_BALL_TARGET_X / 2.1);
        int targetPosY = (int) (COLLECTING_BALL_TARGET_Y / 2.1);
        TaskQueue queue = new TaskQueue();
        queue.add(getNewTurnForColorTask());
        queue.add(getNewMoveUntilColorTask());
        queue.add(getNewLowerBarTask());
        queue.addAll(getNewMoveToWithoutObstacleAvoidTaskQueue(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, targetPosX, targetPosY));
        queue.add(getNewRaiseBarTask());
        //queue.addAll(getNewMoveToWithoutObstacleAvoidTaskQueue(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, 0, 0));
        queue.add(new Task(0,0,OdometryManager.getInstance().getCurrentPosition(), new TaskExecution() {
            @Override
            public void execution(Task t) {
                TaskManager.getInstance().colorBroughtBack();
            }
        }, new TaskCondition(){

            @Override
            public boolean taskFinishCondition() {
                return true;
            }
        }));
        return queue;
    }

    public static TaskQueue getNewExploreWorkspaceForColorTaskQueue(int workspaceFromX, int workspaceFromY, int workspaceToX, int workspaceToY, int numSubspaces){
        int speed = STANDARD_MOVE_SPEED;
        TaskQueue queue = new TaskQueue();

        WorldPoint[][] subspacePoints = getSubspacesWorldpoints(workspaceFromX, workspaceFromY, workspaceToX, workspaceToY, numSubspaces);
        for (int i = 0; i <  Math.sqrt(numSubspaces); i++){
            for (int j = 0; j <  Math.sqrt(numSubspaces); j++) {
                queue.addAll(getNewColorAwareMoveToTask(speed, speed, (int) subspacePoints[i][j].getX(), (int) subspacePoints[i][j].getY()));
                queue.add(getNewTurnOnceForColorTask());
            }
        }
        return queue;
    }

    public static WorldPoint[][] getSubspacesWorldpoints(int workspaceFromX, int workspaceFromY, int workspaceToX, int workspaceToY, int numSubspaces){
        WorldPoint[][] points = new WorldPoint[numSubspaces/2][numSubspaces/2];

        if (numSubspaces % 2 != 0) {
            Log.e("TASK", "Number of subspaces must be a multiple of 2!");
            return null;
        }

        int lengthWorkspaceX = Math.abs(workspaceToX - workspaceFromX);
        int lengthWorkspaceY = Math.abs(workspaceToY - workspaceFromY);

        for (int i = 0; i < numSubspaces/2; i++){
            for (int j = 0; j < numSubspaces/2; j++){
                float px = ((2*i)+1) * lengthWorkspaceX / numSubspaces;
                float py = ((2*j)+1) * lengthWorkspaceY / numSubspaces;

                px -= Math.abs(workspaceFromX);
                py -= Math.abs(workspaceFromY);
                points[i][j] = new WorldPoint(px, py);
            }
        }
        return points;
    }

    public static TaskQueue getNewCollectColorTaskQueue(int maxBalls, int ballsPerRun, int targetX, int targetY){

        TaskQueue queue = new TaskQueue();

        COLLECTING_BALL_TARGET_X = targetX;
        COLLECTING_BALL_TARGET_Y = targetY;

        int numRuns = maxBalls / ballsPerRun;

        for (int j = 0; j < numRuns; j++) {

            for (int i = 0; i < ballsPerRun; i++) {
                queue.addAll(getNewCollectBallTaskQueue());
            }
           // queue.addAll(getNewMoveToWithoutObstacleAvoidTaskQueue(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, targetX, targetY));
            //queue.add(getNewRaiseBarTask());

        }

        queue.addAll(getNewMoveToWithoutObstacleAvoidTaskQueue(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, 0, 0));
        return queue;
    }

    public static TaskQueue getNewCollectBallTaskQueue(){
        TaskQueue queue = new TaskQueue();
        queue.addAll(getNewExploreWorkspaceForColorTaskQueue(-50,-50,50,50,4));
        /*queue.add(getNewRaiseBarTask());
        queue.add(getNewMoveUntilColorTask());
        queue.add(getNewLowerBarTask());*/
        return queue;
    }

    public static TaskQueue getNewColorAwarePath(){
        TaskQueue queue = new TaskQueue();
        int size1 = 25;
        int size2 = 50;
        int size3 = 75;
        int size4 = 100;
        int size5 = 125;

        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, size1,0));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED,STANDARD_MOVE_SPEED, size1, size1));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, 0, size1));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, 0, 0));

        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, size2,0));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED,STANDARD_MOVE_SPEED, size2, size2));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, 0, size2));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, 0, 0));

        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, size3,0));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED,STANDARD_MOVE_SPEED, size3, size3));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, 0, size3));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, 0, 0));

        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, size4,0));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED,STANDARD_MOVE_SPEED, size4, size4));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, 0, size4));
        queue.addAll(getNewColorAwareMoveToTask(STANDARD_MOVE_SPEED, STANDARD_MOVE_SPEED, 0, 0));
        return queue;
    }

    public static TaskQueue getNewColorAwareMoveToTask(int velR, int velL, int x, int y){
        TaskQueue queue = new TaskQueue();

        Task turnTask = getNewColorAwareTurnToTask(velR, velL, x, y);
        queue.add(turnTask);


        Task moveTask = getNewColorAwareMoveTask(velR, velL, x, y);

        queue.add(moveTask);

        return queue;
    }

    public static Task getNewColorAwareTurnTask(int degree){
        RobotPosVector target = new RobotPosVector(OdometryManager.getInstance().getCurrentPosition().getX(), OdometryManager.getInstance().getCurrentPosition().getY(), OdometryManager.getInstance().getCurrentPosition().getAngle());
        target.addAngle(degree);
        return new Task(STANDARD_TURN_SPEED,-STANDARD_TURN_SPEED, target, getStandardTurnTaskExecution(),getStandardColorAwareTurnTaskCondition());
    }

    public static Task getNewColorAwareMoveTask(int velR, int velL, int x, int y){

        RobotPosVector currPos = OdometryManager.getInstance().getCurrentPosition();
       RobotPosVector target = new RobotPosVector(currPos.getX(), currPos.getY(), currPos.getAngle());
        target.add( new RobotPosVector(x, y, 0));

        return new Task(velR, velL, target, getStandardMoveTaskExecution(), getStandardColorAwareTaskCondition());
    }


    public static Task getNewTurnForColorTask(){
        return new Task(STANDARD_TURN_SPEED,-STANDARD_TURN_SPEED,OdometryManager.getInstance().getCurrentPosition(),getStandardMoveTaskExecution(), getColorSearchTaskCondition());
    }

    public static Task getNewTurnOnceForColorTask(){

        return new Task(STANDARD_TURN_SPEED,-STANDARD_TURN_SPEED, OdometryManager.getInstance().getCurrentPosition(), getStandardColorAwareTurnOnceTaskExecution(), getStandardColorAwareTurnOnceTaskCondition());
    }

    public static Task getNewMoveUntilColorTask(){
        return new Task(STANDARD_MOVE_SPEED,STANDARD_MOVE_SPEED,OdometryManager.getInstance().getCurrentPosition(),getStandardMoveTaskExecution(), getColorInRangeTaskCondition());
    }

    public static Task getNewLowerBarTask(){
        return new Task(0,0, OdometryManager.getInstance().getCurrentPosition(), new TaskExecution() {
            @Override
            public void execution(Task t) {
                ControlManager.getInstance().robotSetBar((byte) 1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },
        new TaskCondition() {
            @Override
            public boolean taskFinishCondition() {
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                return true;
            }
        });
    }

    public static Task getNewRaiseBarTask(){
        return new Task(0,0, OdometryManager.getInstance().getCurrentPosition(), new TaskExecution() {
            @Override
            public void execution(Task t) {
                ControlManager.getInstance().robotSetBar((byte) -1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },
                new TaskCondition() {
                    @Override
                    public boolean taskFinishCondition() {
                        /*try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        return true;
                    }
                });
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

    public static TaskCondition getStandardColorAwareTaskCondition(){
        return new TaskCondition() {
            @Override
            public boolean taskFinishCondition() {
                if (CameraManager.getInstance().checkColorInScreen()){
                    TaskManager.getInstance().colorInScreenCallback();
                    return true;
                }
                if (OdometryManager.getInstance().getCurrentPosition().isAt(OdometryManager.getInstance().getTargetPosition())){
                    //OdometryManager.getInstance().setCurrentPosition(OdometryManager.getInstance().getTargetPosition());
                    TaskManager.getInstance().targetReachedCallback();
                    return true;
                }
                return false;
            }
        };
    }

    public static TaskCondition getStandardColorAwareTurnTaskCondition(){
        return new TaskCondition() {
            @Override
            public boolean taskFinishCondition() {
                if (CameraManager.getInstance().checkColorInScreen()){
                    TaskManager.getInstance().colorInScreenCallback();
                    return true;
                }
                if (OdometryManager.getInstance().getCurrentPosition().isAtWithAngle(OdometryManager.getInstance().getTargetPosition())){
                    OdometryManager.getInstance().setCurrentPosition(OdometryManager.getInstance().getTargetPosition());
                    TaskManager.getInstance().targetReachedCallback();
                    return true;
                }
                return false;
            }
        };
    }

    public static TaskCondition getStandardColorAwareTurnOnceTaskCondition(){
        return new TaskCondition() {
            @Override
            public boolean taskFinishCondition() {
                if (CameraManager.getInstance().checkColorInScreen()){
                    TaskManager.getInstance().colorInScreenCallback();
                    return true;
                }
                if (OdometryManager.getInstance().getCurrentPosition().isAtWithAngle(OdometryManager.getInstance().getTargetPosition())){
                    OdometryManager.getInstance().setCurrentPosition(OdometryManager.getInstance().getTargetPosition());

                    TaskManager.getInstance().targetReachedCallback();
                    return true;
                }
                return false;
            }
        };
    }

    public static TaskExecution getStandardColorAwareTurnOnceTaskExecution(){
        return new TaskExecution() {
                @Override
                public void execution(Task t) {

                    t.target = new RobotPosVector(OdometryManager.getInstance().getCurrentPosition().getX(),
                    OdometryManager.getInstance().getCurrentPosition().getY(), OdometryManager.getInstance().getCurrentPosition().getAngle());
                    t.target.addAngle(359);

                    OdometryManager.getInstance().setTargetPosition(t.getTarget());
                    OdometryManager.getInstance().setEventCallback(TaskManager.getInstance());
                    ObstacleAvoidManager.getInstance().setEventCallback(TaskManager.getInstance());
                    MainActivity.getInstance().joinManagerThreads();
                    ControlManager.getInstance().robotSetVelocity((byte) t.getVelocityLeft(), (byte) t.getVelocityRight());
                    MainActivity.getInstance().startManagers();
                }
            };
        };


    public static TaskCondition getMoveWithoutObstacleAvoidTaskCondition(){
        return new TaskCondition() {
            @Override
            public boolean taskFinishCondition() {
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
                return CameraManager.getInstance().checkColorInMiddle() && !CameraManager.getInstance().checkColorInRange();
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
                    OdometryManager.getInstance().setCurrentPosition(OdometryManager.getInstance().getTargetPosition());
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
                int turnSpeed = STANDARD_TURN_SPEED;
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
