package com.example.damihl.robotmove.controls;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;

import com.example.damihl.robotmove.connection.ConnectionManager;

import jp.ksksue.driver.serial.FTDriver;


/**
 * Created by dAmihl on 09.03.15.
 */
public class ControlManager {

    private Activity application;
    private FTDriver ftDriver;
    private ConnectionManager con;
    static int BAUDRATE = 7200;

    public ControlManager(ConnectionManager conn){
       this.con = conn;
       this.ftDriver = this.con.getDriver();
    }




    public void robotSetBar(byte value) {
        comReadWrite(
                new byte[] { 'o', value, '\r', '\n' }
        );
    }




    public void robotSetVelocity(byte left, byte right) {
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






    public void comWrite(byte[] data) {
        if (ftDriver.isConnected()) {
            ftDriver.write(data);
        } else {
            // textLog.append("not connected\n");
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
