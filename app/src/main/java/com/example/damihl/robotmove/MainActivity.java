package com.example.damihl.robotmove;

import java.sql.Connection;
import java.util.Locale;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



import com.example.damihl.robotmove.connection.ConnectionManager;
import com.example.damihl.robotmove.controls.ControlManager;
import com.example.damihl.robotmove.obstacleavoidance.ObstacleAvoidManager;
import com.example.damihl.robotmove.odometry.OdometryManager;
import com.example.damihl.robotmove.paths.PathDriveManager;
import com.example.damihl.robotmove.tasks.Task;
import com.example.damihl.robotmove.tasks.TaskManager;
import com.example.damihl.robotmove.tasks.TaskQueue;
import com.example.damihl.robotmove.uifragments.ControlFragment;
import com.example.damihl.robotmove.uifragments.CoordMoveFragment;
import com.example.damihl.robotmove.uifragments.LogFragment;
import com.example.damihl.robotmove.uifragments.OdometryFragment;
import com.example.damihl.robotmove.uifragments.PathsFragment;
import com.example.damihl.robotmove.uifragments.SensorFragment;
import com.example.damihl.robotmove.utils.RobotPosVector;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private static final int NUM_TABS = 6;

    private static MainActivity instance = null;


    public static MainActivity getInstance(){
        return instance;
    }

    private static final Integer FRAGMENT_CONTROL_INDEX = 0;
    private static final Integer FRAGMENT_LOG_INDEX = 1;
    private static final Integer FRAGMENT_ODOMETRY_INDEX = 2;
    private static final Integer FRAGMENT_SENSOR_INDEX = 3;
    private static final Integer FRAGMENT_COORD_MOVE_INDEX = 4;
    private static final Integer FRAGMENT_PATHS_INDEX = 5;

    private ConnectionManager connectionManager;
    private ControlManager controlManager;
    private ObstacleAvoidManager obstacleManager;
    private PathDriveManager pathManager;
    private OdometryManager odometryManager;


    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(NUM_TABS);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        instance = this;
        init();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

           Fragment f;

            if (position == FRAGMENT_CONTROL_INDEX){
                return ControlFragment.newInstance(position);
            }else if (position == FRAGMENT_LOG_INDEX){
                return LogFragment.newInstance(position);
            }else if (position == FRAGMENT_ODOMETRY_INDEX){
                return OdometryFragment.newInstance(position);
            }else if (position == FRAGMENT_SENSOR_INDEX){
                return SensorFragment.newInstance(position);
            }else if (position == FRAGMENT_COORD_MOVE_INDEX){
                return CoordMoveFragment.newInstance(position);
            }else if (position == FRAGMENT_PATHS_INDEX){
                return PathsFragment.newInstance(position);
            }else{
                return PlaceholderFragment.newInstance(position + 1);
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return NUM_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
                case 4:
                    return getString(R.string.title_section5).toUpperCase(l);
                case 5:
                    return getString(R.string.title_section6).toUpperCase(l);

            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
            return rootView;
        }
    }


    /*

   *****************************************************************************************
   * BUSINESS LOGIC FUNCTIONS!!!
   * **********************************************************************++
     */



    private void init(){
        this.connectionManager = ConnectionManager.getInstance();
        this.connectionManager.initUSB();
        this.odometryManager = OdometryManager.getInstance();
        this.controlManager = ControlManager.getInstance();
        this.obstacleManager = ObstacleAvoidManager.getInstance();
        this.pathManager = PathDriveManager.getInstance();

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
            if (checkConnection()) {
                printDebugText("driving");
                moveStandard();
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
            TaskQueue squareTask = PathDriveManager.getInstance().getSquareTestPath(2000);
            TaskManager.getInstance().executeTaskQueue(squareTask);
        }
    }

   public void onButtonMoveTowardsClick(View v){
        if (checkConnection()){
            TextView xCoord = (TextView) findViewById(R.id.moveToX);
            TextView yCoord = (TextView) findViewById(R.id.moveToY);

            int x = 2000;
            int y = 2000;

           /* if (xCoord.getTextSize() > 0) {
                x = Integer.parseInt(xCoord.getText().toString());
                y = Integer.parseInt(yCoord.getText().toString());
            }else{
                x = 2000;
                y = 2000;
            }*/
            TaskQueue moveTowardsTask = Task.getNewMoveToTaskQueue(15, 15, x, y);
            TaskManager.getInstance().executeTaskQueue(moveTowardsTask);
        }
    }


    public void startManagers(){
        odometryManager.startOdometry(ControlManager.getInstance(), instance);
        obstacleManager.startObstacleDetection(ControlManager.getInstance(), instance);
    }

    public void moveStandard(){
        controlManager.robotSetVelocity((byte) 15, (byte) 15);
        startManagers();
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



    public void threadSafeOdometryDataOutput(final RobotPosVector v){
        runOnUiThread(new Runnable(){

            @Override
            public void run() {
                setOdometryData(v.x, v.y, v.angle);
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
