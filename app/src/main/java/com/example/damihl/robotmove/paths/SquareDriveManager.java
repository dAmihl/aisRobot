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
    }

    public void driveSquare(int squareSize){
        if (!activity.checkConnection()) return;

        controlManager.robotDrive((byte) squareSize);
        controlManager.robotTurn((byte) 90);
        controlManager.robotDrive((byte) squareSize);
        controlManager.robotTurn((byte) 90);
        controlManager.robotDrive((byte) squareSize);
        controlManager.robotTurn((byte) 90);
        controlManager.robotDrive((byte) squareSize);
        controlManager.robotTurn((byte) 90);

    }

}
