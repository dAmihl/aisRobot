package com.example.damihl.robotmove.connection;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.example.damihl.robotmove.MainActivity;

import jp.ksksue.driver.serial.FTDriver;


/**
 * Created by dAmihl on 09.03.15.
 */
public class ConnectionManager {

    private MainActivity application;
    private FTDriver com;
    static int BAUDRATE = 9600;

    public ConnectionManager(MainActivity app){
        this.application = app;
    }


    public boolean initUSB(){
        com = new FTDriver((UsbManager) application.getSystemService(Context.USB_SERVICE));
        return true;
    }

    public boolean connect(){
            if (com.begin(9600)) {
                application.printDebugText("connected");
            } else {
                application.printDebugText("could not connect");
            }
        return com.isConnected();
    }


    public void disconnect() {
        com.end();
        if (!com.isConnected()) {
            application.printDebugText("disconnected");
        }
    }


    public FTDriver getDriver(){
        return com;
    }


}
