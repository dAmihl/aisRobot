package com.example.damihl.robotmove.odometry;

import android.app.Activity;
import android.os.SystemClock;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;

import java.util.ResourceBundle;

/**
 * Created by dAmihl on 23.03.15.
 */
public class OdometryManager {

    private MainActivity application;

    private Thread odoThread;

    private final float r = 4.5f;
    private final float d = 20f;
    public final float dt = 0.1f;

    // current robot position and angle
    private float angle;
    private float x;
    private float y;

    //linear velocity
    private float vl;
    private float vr;
    private float vRobot;

    //angular velocity
    private float wr;
    private float wl;
    private float wRobot;


    public OdometryManager(MainActivity app){
        initState();
        this.application = app;
    }

    public void initOdoThread(final MainActivity appl, final ControlManager control){
        odoThread = new Thread(new Runnable(){

            @Override
            public void run() {
                while(control.ROBOT_MOVING) {
                        update();
                        try {
                            Thread.sleep(1000);
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

        angle = 0;
    }

    public void update(){

        vl = r*wl;
        vr = r*wr;
        vRobot = (vr + vl)/2;
        wRobot = (vr - vl)/d;

        x = x + (vRobot * (float) Math.cos(angle));
        y = y + (vRobot * (float) Math.sin(angle));
        angle = angle + (wRobot * dt);

        application.threadSafeDebugOutput("Current Robot position: " + x + "/" + y + "/" + angle);
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



}
