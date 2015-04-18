package com.example.damihl.robotmove.controls;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.connection.ConnectionManager;
import com.example.damihl.robotmove.odometry.OdometryManager;
import com.example.damihl.robotmove.utils.RobotPosVector;

import java.sql.Connection;

import jp.ksksue.driver.serial.FTDriver;


 // ToDo: put the api overview in a table
 // -> html
/**
 * Created by dAmihl on 09.03.15.
 * <br />
 * <br />
 * <b>
 * ControlManager:
 * </b>
 * <br />
 * This singleton class provides an interface to the basic functions of th robot <br />
 * The functionality provided by this class includes: <br />
 * <hr />
 * &nbsp;&nbsp;<i>robotSetBar()</i> - move the bar of the robot<br />
 * &nbsp;&nbsp;<i>robotSetVelocity()- let robot move w\ a certain velocity</i> <br />
 * &nbsp;&nbsp;<i>robotSetLeds()</i> - control the robots blue and red leds<br />
 * &nbsp;&nbsp;<i>robotStop() - stop the robot</i> <br />
 * &nbsp;&nbsp;<i>robotTurn() - turn the robot a certain angle</i> <br />
 * &nbsp;&nbsp;<i>robotDrive() - let the robot drive a given distance in cm</i> <br />
 * &nbsp;&nbsp;<i>getSensorData() - returns the read sensor data</i> <br />
 * &nbsp;&nbsp;<i>pause() - lets the ControlManager rest for a specified time in ms</i>
 * <hr />
 * <br />
 * further functions are: <br />
 * <hr />
 * <i>comRead()</i>
 * <i>comWrite()</i>
 * <i>comReadWrite()</i>
 * <hr />
 *
 */
public class ControlManager {

    private static ControlManager instance = null;

    public static ControlManager getInstance(){
        if (instance != null) return instance;
        else{
            instance = new ControlManager();
            return instance;
        }
    }


    private MainActivity application;
    private OdometryManager odometryManager;
    private FTDriver ftDriver;
    private ConnectionManager con;

    public boolean ROBOT_MOVING = false;

    private ControlManager(){
       this.con = ConnectionManager.getInstance();
        this.odometryManager = OdometryManager.getInstance();
       this.ftDriver = this.con.getDriver();
    }


     /**
      * move the bar of the robot
      * @param value
      */
    public void robotSetBar(byte value) {
        comReadWrite(
                new byte[] { 'o', value, '\r', '\n' }
        );
    }


    public void robotStop(){
        robotSetVelocity((byte) 0, (byte) 0);
    }



    public void robotSetVelocity(byte left, byte right) {
        odometryManager.setAngularVelocities((int) left, (int) right);
        ROBOT_MOVING = ((int) left != 0 || (int) right != 0);
        comReadWrite(
                new byte[] { 'i', left, right, '\r', '\n' }
        );
    }




    public void targetReached() {
        robotStop();
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
               application.threadSafeDebugOutput(e.toString());
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
