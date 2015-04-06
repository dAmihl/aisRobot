package com.example.damihl.robotmove.odometry;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.tasks.TaskManager;
import com.example.damihl.robotmove.utils.EventCallback;
import com.example.damihl.robotmove.utils.RobotPosVector;

/**
 * Created by dAmihl on 23.03.15.
 */
public class OdometryManager {

    private static OdometryManager instance = null;

    private MainActivity application;

    private Thread odoThread;

    private final float r = 4.5f;
    private final float d = 20f;
    public final float dt = 0.1f;
    public final long sleepTime = 100;

    private boolean hasTarget = false;
    private RobotPosVector targetPosition = null;
    private EventCallback targetReachEventCallback = null;

    // current robot position and angle
    RobotPosVector currentPosition;


    //linear velocity
    private float vl;
    private float vr;
    private float vRobot;

    //angular velocity
    private float wr;
    private float wl;
    private float wRobot;


    public static OdometryManager getInstance(){
        if (instance != null) return instance;
        else return new OdometryManager();
    }


    private OdometryManager(){
        currentPosition = new RobotPosVector(0,0,0);
        initState();
        this.application = MainActivity.getInstance();
        this.targetReachEventCallback = TaskManager.getInstance();
    }

    public void initOdoThread(final MainActivity appl, final ControlManager control){
        odoThread = new Thread(new Runnable(){

            @Override
            public void run() {
                while(control.ROBOT_MOVING) {
                        update();
                        if (hasTarget){
                            checkTargetReached();
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



    public void initState(){
        vl = 0;
        vr = 0;
        vRobot = 0;

        wr = 0;
        wl = 0;
        wRobot = 0;

    }

    public void update(){

        vl = r*wl;
        vr = r*wr;
        vRobot = (vr + vl)/2;
        wRobot = (vr - vl)/d;

        currentPosition.x = currentPosition.x + (vRobot * (float) Math.cos(Math.toRadians(currentPosition.angle)));
        currentPosition.y = currentPosition.y + (vRobot * (float) Math.sin(Math.toRadians(currentPosition.angle)));
        currentPosition.angle = currentPosition.angle + (wRobot * dt);

       application.threadSafeOdometryDataOutput(currentPosition);

    }

    public void setAngularVelocities(float left, float right){
        wl = left;
        wr = right;
    }

    public void startOdometry(final ControlManager control, final MainActivity appl){
        try {
            initOdoThread(appl, control);
            odoThread.start();
        }catch(Exception e){
            appl.printDebugText("odometry error: "+e);
        }
        appl.printDebugText("odometry started");
    }


    public void setTargetPosition(RobotPosVector target){
        hasTarget = true;
        this.targetPosition = target;
    }

    public void setEventCallback(EventCallback called){
        this.targetReachEventCallback = called;
    }


    private void checkTargetReached(){
        if (currentPosition.isAt(targetPosition)){
            targetReached();
        }
    }

    private void targetReached(){
        hasTarget = false;
        this.targetPosition = null;
        targetReachEventCallback.targetReachedCallback();
        this.targetReachEventCallback = null;
    }


    public RobotPosVector getCurrentPosition(){
        return currentPosition;
    }

}
