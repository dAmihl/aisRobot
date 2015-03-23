package com.example.damihl.robotmove.controls;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.connection.ConnectionManager;
import com.example.damihl.robotmove.odometry.OdometryManager;

import jp.ksksue.driver.serial.FTDriver;


/**
 * Created by dAmihl on 09.03.15.
 */
public class ControlManager {

    private MainActivity application;
    private OdometryManager odometryManager;
    private FTDriver ftDriver;
    private ConnectionManager con;

    public boolean ROBOT_MOVING = false;

    public ControlManager(ConnectionManager conn, OdometryManager odo){
       this.con = conn;
        this.odometryManager = odo;
       this.ftDriver = this.con.getDriver();
    }




    public void robotSetBar(byte value) {
        comReadWrite(
                new byte[] { 'o', value, '\r', '\n' }
        );
    }




    public void robotSetVelocity(byte left, byte right) {
        odometryManager.setAngularVelocities((int) left, (int) right);
        ROBOT_MOVING = ((int) left != 0 || (int) right != 0);
        comReadWrite(
                new byte[] { 'i', left, right, '\r', '\n' }
        );
    }




    public void robotTurn(byte degree) {
        comReadWrite(
                new byte[] { 'l', degree, '\r', '\n' }
        );
    }




    public void robotDrive(byte distance_cm) {
       comReadWrite(
                new byte[] { 'k', distance_cm, '\r', '\n' }
        );
    }




    public void robotSetLeds(byte red, byte blue) {
        comReadWrite(
                new byte[] { 'u', red, blue, '\r', '\n' }
        );
    }

    public void robotDriveObstacleSafe(){

    }


    public String getSensorData() {
        String data = comReadWrite(new byte[] { 'q', '\r', '\n' });
        return data;
    }


    public void pause(int ms){
        try {
            Thread.sleep(ms);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }



    public int getSleepTimeRobotDrive(int distance){
        int waitTimeMove = 1000 * distance / 20;
        return waitTimeMove;
    }

    public int getSleepTimeRobotTurn(int degrees){
        int waitTimeTurn = 1000;
        return waitTimeTurn;
    }

    public int computeCorrectDegree(int degrees){
        return (int)(degrees * 1.1);
    }



    public void comWrite(byte[] data) {
        if (ftDriver.isConnected()) {
            try{
                ftDriver.write(data);
            } catch (Exception e){
               application.printDebugText(e.toString());
            }
        } else {
        }
    }

    public String comRead() {
        String s = "";
        int i = 0;
        int n = 0;
        while (i < 3 || n > 0) {
            byte[] buffer = new byte[256];
            n = ftDriver.read(buffer);
            s += new String(buffer, 0, n);
            i++;
        }
        return s;
    }

    public String comReadWrite(byte[] data) {
        ftDriver.write(data);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
        return comRead();
    }


}
