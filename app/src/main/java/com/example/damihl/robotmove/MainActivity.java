package com.example.damihl.robotmove;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.damihl.robotmove.connection.ConnectionManager;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.paths.SquareDriveManager;

public class MainActivity extends ActionBarActivity {

    private ConnectionManager connectionManager;
    private ControlManager controlManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        this.connectionManager = new ConnectionManager(this);
        this.connectionManager.initUSB();
        this.controlManager = new ControlManager(connectionManager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonConnectClick(View v){
        if (this.connectionManager.getDriver().isConnected()){
            connectionManager.disconnect();
        }else{
            connectionManager.connect();
        }
        checkConnection();
    }

    public void onButtonMoveClick(View v){
         if (checkConnection())
             controlManager.robotDrive((byte) 10);
    }

    public void onButtonMoveBackClick(View v){
        if (checkConnection())
            controlManager.robotDrive((byte) -10);
    }

    public void onButtonStopClick(View v){
        if (checkConnection())
            controlManager.robotSetVelocity((byte) 0, (byte) 0);
    }

    public void onButtonTurnLeftClick(View v){
        if (checkConnection())
            controlManager.robotTurn((byte) 90);
    }

    public void onButtonTurnRightClick(View v){
        if (checkConnection())
            controlManager.robotTurn((byte) -90);
    }

    public void onButtonBarUpClick(View v){
        if (checkConnection())
            controlManager.robotSetBar((byte) -1);
    }

    public void onButtonBarDownClick(View v){
        if (checkConnection())
            controlManager.robotSetBar((byte) 1);
    }

    public void onButtonDriveSquareClick(View v){
        if (checkConnection()){
            SquareDriveManager man = new SquareDriveManager(controlManager, this);
            man.driveSquare(100);
        }


    }




    /*
    HELPER FUNCTIONS
     */
    public void printDebugText(String db){
        TextView debugText = (TextView)
                findViewById(R.id.debugText);
        //debugText.setText(db);
        debugText.append(db+"\n");
    }

    public boolean checkConnection(){
        TextView connection = (TextView) findViewById(R.id.textConnectionStatus);
        if (connectionManager.getDriver().isConnected()){
            connection.setText("connected");
            return true;
        }else{
            connection.setText("disconnected");
            printDebugText("not connected");
            return false;
        }
    }

}
