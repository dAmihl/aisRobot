package com.example.damihl.robotmove;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.damihl.robotmove.connection.ConnectionManager;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.obstacleavoidance.ObstacleAvoidManager;
import com.example.damihl.robotmove.odometry.OdometryManager;
import com.example.damihl.robotmove.paths.PathDriveManager;


public class MainActivity extends ActionBarActivity {

    private ConnectionManager connectionManager;
    private ControlManager controlManager;
    private ObstacleAvoidManager obstacleManager;
    private PathDriveManager pathManager;
    private OdometryManager odometryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        initTabs();
        init();

    }

    private void initTabs(){
        TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec spec1=tabHost.newTabSpec("General");
        spec1.setContent(R.id.generalTab);
        spec1.setIndicator("General");

        TabHost.TabSpec spec2=tabHost.newTabSpec("Log");
        spec2.setIndicator("Log");
        spec2.setContent(R.id.logTab);

        TabHost.TabSpec spec3=tabHost.newTabSpec("Odometry");
        spec3.setIndicator("Odometry");
        spec3.setContent(R.id.odometryTab);


        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);

    }



    private void init(){
        this.connectionManager = new ConnectionManager(this);
        this.connectionManager.initUSB();
        this.odometryManager = new OdometryManager(this);
        this.controlManager = new ControlManager(connectionManager, odometryManager);
        this.obstacleManager = new ObstacleAvoidManager(controlManager, this);
        this.pathManager = new PathDriveManager(controlManager, this, obstacleManager);

        // this.odometryManager.initOdoThread(this, controlManager);


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
        if (checkConnection()){
            printDebugText("driving");
            controlManager.robotSetVelocity((byte) 15,(byte) 15);
            odometryManager.startOdometry(controlManager, this);
            obstacleManager.startObstacleDetection(controlManager, this);
        }
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
            //pathManager.driveSquare(100);
            pathManager.driveSquareObstacleSafe(100);
        }
    }






    /*
    HELPER FUNCTIONS
     */


    public void threadSafeSensorDataOutput(final String[] dataArr){
        runOnUiThread(new Runnable(){

            @Override
            public void run() {
                outputSensorData(dataArr);
            }
        });
    }



    public void outputSensorData(String[] dataArr){
        TextView sensorText = (TextView) findViewById(R.id.sensorText);
        String output = "SensorData: ";
        for (String s: dataArr){
            output += s + " // ";
        }
        sensorText.setText(output);
    }



    public void threadSafeOdometryDataOutput(final float x, final float y, final float angle){
        runOnUiThread(new Runnable(){

            @Override
            public void run() {
                setOdometryData(x, y, angle);
            }
        });
    }


    public void setOdometryData(float x, float y, float angle){
        TextView odometryTextX = (TextView) findViewById(R.id.odometryTextX);
        TextView odometryTextY = (TextView) findViewById(R.id.odometryTextY);
        TextView odometryTextAngle = (TextView) findViewById(R.id.odometryTextAngle);

        odometryTextX.setText("X: "+x);
        odometryTextY.setText("Y: "+y);
        odometryTextAngle.setText("Angle: "+angle);
    }



    public void threadSafeDebugOutput(final String db){
        runOnUiThread(new Runnable(){

            @Override
            public void run() {
                printDebugText(db);
            }
        });
    }

    public void printDebugText(String db){
        TextView debugText = (TextView)
                findViewById(R.id.debugText);
        //debugText.setText(db);
        //debugText.append(new Date()+": "+db+"\n");
        debugText.append(db+"\n");
        //debugText.setText(db);
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
