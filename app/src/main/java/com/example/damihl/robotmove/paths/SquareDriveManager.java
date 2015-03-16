package com.example.damihl.robotmove.paths;

import com.example.damihl.robotmove.MainActivity;
import com.example.damihl.robotmove.controls.ControlManager;

/**
 * Created by dAmihl on 13.03.15.
 */
public class SquareDriveManager {

    private ControlManager controlManager;
    private MainActivity activity;

    public SquareDriveManager(ControlManager con, MainActivity act){
        this.controlManager = con;
        this.activity = act;
    }

    public void driveSquare(int squareSize){
        if (!activity.checkConnection()) return;

        int waitTimeMove = 1000 * squareSize / 20;
        int waitTimeTurn = 1000;
        int turnsize = 99;

        controlManager.robotDrive((byte) squareSize);
        pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);
        pause(waitTimeTurn);
        controlManager.robotDrive((byte) squareSize);
        pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);
        pause(waitTimeTurn);
        controlManager.robotDrive((byte) squareSize);
        pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);
        pause(waitTimeTurn);
        controlManager.robotDrive((byte) squareSize);
        pause(waitTimeMove);
        controlManager.robotTurn((byte) turnsize);

    }

    private void pause(int ms){
        try {
            Thread.sleep(ms);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}
