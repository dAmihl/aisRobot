package com.example.damihl.robotmove.odometry;

import android.util.Log;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.tasks.TaskManager;
import com.example.damihl.robotmove.utils.EventCallback;
import com.example.damihl.robotmove.utils.RobotPosVector;

/**
 * <br />
 * Created by dAmihl on 23.03.15. <br />
 * <br />
 * <br />
 * <b>
 * OdometryManager:
 * </b>
 * <br />
 * This singleton class keeps track of the robots current position<br />
 */
public class OdometryManager {

    private static OdometryManager instance = null;

    private MainActivity application;

    private Thread odoThread;

    private final float r = 4.5f;
    private final float d = 20f;
    private final float dt = 100f;
    private final float ANGLE_CALIBRATION_FACTOR = 1.23f;
    private final float u = (float) (2*r*Math.PI);
   // public final long sleepTime = (long) (1000 * dt);
    public final long sleepTime = 100;

    // target
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
        else{
            instance = new OdometryManager();
            return instance;
        }
    }


    private OdometryManager(){
        currentPosition = new RobotPosVector(0,0,0);
        initState();
        this.application = MainActivity.getInstance();
        this.targetReachEventCallback = TaskManager.getInstance();
    }

    public void joinThread() throws InterruptedException {
        if (this.odoThread != null)
            this.odoThread.join();
    }

    public void initOdoThread(final MainActivity appl, final ControlManager control){
        odoThread = new Thread(new Runnable(){

            @Override
            public void run() {
                while(control.ROBOT_MOVING) {
                        update();
                        try {
                            Thread.sleep(sleepTime);
                        } catch (Exception e) {
                            appl.threadSafeDebugOutput(e.toString());
                        }
                }
            }

        });
    }


    /* initialize the robots position with (0,0,0)*/
    public void initState(){
        vl = 0;
        vr = 0;
        vRobot = 0;

        wr = 0;
        wl = 0;
        wRobot = 0;

    }

    public void resetOdometry(){
        initState();
        currentPosition = new RobotPosVector(0,0,0);
        MainActivity.getInstance().setOdometryData(currentPosition.x, currentPosition.y, currentPosition.getAngle());
    }

    public void setCurrentPosition(RobotPosVector currentPos){
        this.currentPosition = currentPos;
    }

    /**
     * this method calculates the current position <br />
     * and writes it into the corresponding fields;
     */
    public void update(){

        vl = r*wl;
        vr = r*wr;
        vRobot = (vr + vl)/2;
        wRobot = (vr - vl)/d;





        float oneRevolutionL = (float)(2*Math.PI/wl);
        float oneRevolutionR = (float)(2*Math.PI/wr);
        float ticksPerRevolutionL = oneRevolutionL / ((float)(sleepTime)/1000);
        float ticksPerRevolutionR = oneRevolutionR / ((float)(sleepTime)/1000);

        float dsl = u / ticksPerRevolutionL;
        float dsr = u / ticksPerRevolutionR;
        //float dphi = (Math.max(dsl, dsr) - Math.min(dsl, dsr))   / (d * ((float)(sleepTime)/1000));
        float dphi = (dsl - dsr)   / (d * ((float)(sleepTime)/1000));


        currentPosition.x = (currentPosition.x + (vRobot * (float) Math.cos(Math.toRadians(currentPosition.getAngle()))) / dt) ;
        currentPosition.y = (currentPosition.y + (vRobot * (float) Math.sin(Math.toRadians(currentPosition.getAngle()))) / dt) ;
//        currentPosition.angle = currentPosition.angle + (wRobot * dt);
        currentPosition.addAngle(dphi * ANGLE_CALIBRATION_FACTOR);
       // Log.i("Angle", "Current Robot Angle UPDATE: "+currentPosition.getAngle());


        application.threadSafeOdometryDataOutput(currentPosition);

    }

    public void setAngularVelocities(float left, float right){
        wl = left;
        wr = right;
    }

    public void startOdometry(final ControlManager control, final MainActivity appl){
        try {
            //while (odoThread.isAlive());

            //initOdoThread(appl, control);
            odoThread.start();
        }catch(Exception e){
            appl.threadSafeDebugOutput("odometry error: "+e);
        }
        appl.threadSafeDebugOutput("odometry started");
    }


    public void setTargetPosition(RobotPosVector target){
        hasTarget = true;
        this.targetPosition = target;
        MainActivity.getInstance().threadSafeDebugOutput("Odometry got target: "+target);
    }

    public void setEventCallback(EventCallback called){
        this.targetReachEventCallback = called;
    }


    public boolean checkTargetReached(){
        if (!hasTarget) return true;
        if (currentPosition.isAtWithAngle(targetPosition)){
            MainActivity.getInstance().threadSafeDebugOutput("Target reached odo: "+targetPosition);
            targetReached();
            return true;
        }
        return false;
    }

    private void targetReached(){
        hasTarget = false;
      //  this.targetPosition = null;
        targetReachEventCallback.targetReachedCallback();
    }

    /**
     * @return
     *      a <i>RobotPosVector</i> with the estimated current Position
     */
    public RobotPosVector getCurrentPosition(){
        return currentPosition;
    }

    /**
     * @return
     *      the <i>RobotPosVector</i> of the target (relative to the start position, which is initialized as (0,0,0))
     */
    public RobotPosVector getTargetPosition(){
        return targetPosition;
    }

}
