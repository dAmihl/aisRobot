package com.example.damihl.robotmove.connection;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.example.damihl.robotmove.MainActivity;

import jp.ksksue.driver.serial.FTDriver;


/**
 * <br />
 * Created by dAmihl on 09.03.15.
 * <br />
 * <br />
 * <b>
 * ConnectionManager:
 * </b>
 * <br />
 * This singleton class manages the connection to the robot <br />
 *
 * Important methodes of this class are: <br />
 * <hr />
 * &nbsp;&nbsp;<i>connect()</i><br />
 * &nbsp;&nbsp;<i>disconnect()</i>
 * <hr />
 * @see ConnectionManager#connect()
 * @see ConnectionManager#disconnect()
 */
public class ConnectionManager {

    private static ConnectionManager instance = null;

    public static ConnectionManager getInstance(){
        if (instance != null) return instance;
        else{
            instance = new ConnectionManager();
            return instance;
        }
    }

    private MainActivity application;
    private FTDriver com;
    static int BAUDRATE = 9600;

    private ConnectionManager(){
        this.application = MainActivity.getInstance();
        initUSB();
    }


    public boolean initUSB(){
        com = new FTDriver((UsbManager) application.getSystemService(Context.USB_SERVICE));
        return true;
    }

    /**
     * this method establish a connection to the robot
     * @return <b>true</b> if successful and <br />
     *         <b>false</b> otherwise
     */
    public boolean connect(){
            if (com.begin(9600)) {
                application.threadSafeDebugOutput("connected");
            } else {
                application.threadSafeDebugOutput("could not connect");
            }
        return com.isConnected();
    }

    /**
     * this method breaks the connection to the robot
     */
    public void disconnect() {
        com.end();
        if (!com.isConnected()) {
            application.threadSafeDebugOutput("disconnected");
        }
    }


    public FTDriver getDriver(){
        return com;
    }


}
