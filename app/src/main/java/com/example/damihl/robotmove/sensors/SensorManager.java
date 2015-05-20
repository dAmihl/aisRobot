package com.example.damihl.robotmove.sensors;

import android.util.Log;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;

/**
 * Created by dAmihl on 15.04.15.
 */
public class SensorManager {

    private static SensorManager instance = null;

    private final static int sleepTime = 500;

    public static SensorManager getInstance(){
        if (instance != null) return instance;
        else {
            instance = new SensorManager();
            return instance;
        }
    }

    public SensorManager(){
        this.controlManager = ControlManager.getInstance();
    }


    private int leftSensorData = 0;
    private int midSensorData = 0;
    private int rightSensorData= 0;


    private ControlManager controlManager;


    private Thread sensorThread;


    public void joinThread() throws InterruptedException {
        if (this.sensorThread != null)
            this.sensorThread.join();
    }

    public void updateSensorData(){

        String sensorData = this.controlManager.getSensorData();

        String arr[] = sensorData.split(" ");
        if (arr.length < 6) return;
        try {
        String strRight = arr[arr.length - 5].substring(2);
        String strLeft = arr[arr.length - 6].substring(2);
        String strMid = arr[arr.length - 4].substring(2);

            int valueRight = Integer.parseInt(strRight, 16);
            int valueMid = Integer.parseInt(strMid, 16);
            int valueLeft = Integer.parseInt(strLeft, 16);
            leftSensorData = valueLeft;
            midSensorData = valueMid;
            rightSensorData = valueRight;
        }catch(Exception e){
            Log.e("SENSOREXCP", e.toString());
        }


        //MainActivity.getInstance().threadSafeSensorDataOutput(arr);


    }

    public int getLeftSensorData(){
        return leftSensorData;
    }

    public int getMidSensorData(){
        return midSensorData;
    }

    public int getRightSensorData(){
        return rightSensorData;
    }



    public void initSensorThread(final MainActivity appl, final ControlManager control){
        sensorThread = new Thread(new Runnable(){

            @Override
            public void run() {
                while(control.ROBOT_MOVING) {
                    updateSensorData();
                    try {
                        Thread.sleep(sleepTime);
                    } catch (Exception e) {
                        appl.threadSafeDebugOutput(e.toString());
                    }
                }
            }

        });
    }

    public void startSensorThread(final ControlManager control, final MainActivity appl){
        try {
            //while (sensorThread.isAlive());

            //initSensorThread(appl, control);
            sensorThread.start();
        }catch(Exception e){
            appl.threadSafeDebugOutput("sensor error: "+e);
        }
        appl.threadSafeDebugOutput("sensor started");
    }

}
