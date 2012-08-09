/*******************************************************************************
 * Copyright 2012 Intel-GE Care Innovations(TM)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.moodmap;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mood.database.DBAdapter;
import com.mood.database.MoodModel;
import com.mood.database.MoodRepository;
import com.mood.models.Mood;
import com.ui.MoodEntryCustumView;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;

public class HomeActivity extends Activity implements
        SharedPreferences.OnSharedPreferenceChangeListener
{
    private float DEVICE_DENSITY = 0;
    private int DEVICE_WIDTH = 0;
    private LinearLayout rootLinearLayout;
    private float tapX = 0, tapY = 0;
    private float tap_X = 0, tap_Y = 0;
    private MoodEntryCustumView cv;

    private ImageView rlButtonInfo;
    // STC 10/13/11 private ImageView btnTrends;
    private Button btnTrends;

    boolean isMoodSelected = false;
    boolean isTwoMoodEntry = false;
    public static boolean isReminderDialogFocused = false;

    public static Context context;
    Mood moodFirst, moodSecond;

    float firstMoodX = 0, firstMoodY = 0, secondMoodX = 0, secondMoodY = 0;
    float scale;

    boolean isAmAtDisplay = false;
    String strIAmAt = "Unknown";
    String strColorName = "";
    private int kLocationSelectedColor = 0xFF111111;
    private int kLocationNotSelectedColor = 0xFF333333;
    private ImageButton myLocButtonWork; // Location on main screen - STC
                                         // 10/17/11
    private ImageButton myLocButtonHome;
    private ImageButton myLocButtonOnTheGo;
    private ImageButton myLocButtonOther;

    // GPS location support - STC 12/8/12
    Location currentLocation = null;
    private float myCurrentAccuracy = 10000.0f;
    int myLocationFixCnt_Fine = 0;
    int myLocationFixCnt_Coarse = 0;
    int myStatusChangeCnt_Fine = 0;
    private LocationManager mLocationManager = null;
    private LocationListener mLocationListener_Fine = null;
    private LocationListener mLocationListener_Coarse = null;
    private boolean mUserClickedLocation = false;
    private int mLocationType = Constants.kUnknownLocation;

    boolean showLocationQuestion = false;

    TextView myHelpTextView; // STC 12/13/11

    private String participantID = "", studyID = "", deviceID = "";
    private SharedPreferences spIDs;

    boolean isNetwork = false;
    Timer t = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        scale = this.getResources().getDisplayMetrics().density;
        setContentView(R.layout.home_screen_layout);
        context = HomeActivity.this;

        getDeviceWidth(); // get device vidth to create view width and height
        initUi(); // initializ the ui
        initDatabase(); // create database instance

        moodFirst = new Mood();
        moodSecond = new Mood();

        /** Reminder to enter the */
        AlarmReceiver.setNotificationTimer(HomeActivity.this);

        // STC 10/19/11 spIDs =
        // getApplicationContext().getSharedPreferences("IDs", MODE_PRIVATE);
        spIDs = PreferenceManager.getDefaultSharedPreferences(this); // STC
        studyID = spIDs.getString("StudyID", "");
        participantID = spIDs.getString("participantID", "");
        deviceID = spIDs.getString("DeviceID", "");

        // just onResume 1/2/12 registerLocationListeners();
    }

    // OnSharedPreferenceChangeListener
    // Called when Preferences change values
    // 10/19/11
    //
    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String prefKey)
    {
        if (prefKey.equals(Constants.PrefKey_StudyID))
        {
            studyID = spIDs.getString("StudyID", "");
        }
        else if (prefKey.equals(Constants.PrefKey_ParticipantID))
        {
            participantID = spIDs.getString("participantID", "");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        isNetwork = Utils.networkStatus(HomeActivity.this);

        myHelpTextView.setText("");
        resetLocationWithOverrideUser(true);
        registerLocationListeners(); // start location resolution - 1/2/12

        /*
         * STC 12/29/11 if(!isNetwork) { AlertDialog.Builder builder = new
         * AlertDialog.Builder(HomeActivity.this);
         * builder.setMessage("No Network connection") .setCancelable(false)
         * .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
         * public void onClick(DialogInterface dialog, int id) { //
         * HomeActivity.this.finish(); } }); AlertDialog alert =
         * builder.create(); alert.show(); }
         */
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initDatabase() {
        DBAdapter.context = this.getBaseContext();
        DBAdapter dbAdapter = new DBAdapter();
        dbAdapter.createDatabse();
        dbAdapter.closeDatabase();
    }

    @Override
    protected void onDestroy() {
        Log.v("Home Act", "OnDestroy");
        if (t != null)
        {
            t.cancel(); // TODO Auto-generated method stub
        }
        super.onDestroy();
    }

    private void initUi() {

        rootLinearLayout = (LinearLayout) findViewById(R.id.rootLayout);
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                DEVICE_WIDTH, DEVICE_WIDTH);
        rootLinearLayout.setLayoutParams(lp);

        cv = (MoodEntryCustumView) findViewById(R.id.graphCustomView);
        cv.setLayoutParams(lp);
        // cv.setPointsColor(getMoodColor());//Set the color to the mood points
        cv.setOnTouchListener(cvOncliClickListener);

        btnTrends = (Button) findViewById(R.id.TrendsButtonLayout);
        btnTrends.setOnClickListener(btnTrendsOnClickListener);
        // STC 10/13/11 btnTrends =(ImageView)
        // findViewById(R.id.TrendsButtonLayout);
        // STC 10/13/11 btnTrends.setOnClickListener(btnTrendsOnClickListener);
        // STC 10/13/11 btnTrends.setVisibility(View.INVISIBLE);

        rlButtonInfo = (ImageView) findViewById(R.id.buttonInfo);
        rlButtonInfo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent mIntent = new Intent(HomeActivity.this, Instructions.class);
                startActivityForResult(mIntent, 123);
            }
        });

        // Location buttons on the bottom - STC 10/17/11
        myLocButtonWork = (ImageButton) findViewById(R.id.workImageButton);
        myLocButtonHome = (ImageButton) findViewById(R.id.homeImageButton);
        myLocButtonOnTheGo = (ImageButton) findViewById(R.id.onGoImageButton);
        myLocButtonOther = (ImageButton) findViewById(R.id.otherImageButton);
        myLocButtonWork.setOnClickListener(onLocationButtonClickListener);
        myLocButtonHome.setOnClickListener(onLocationButtonClickListener);
        myLocButtonOnTheGo.setOnClickListener(onLocationButtonClickListener);
        myLocButtonOther.setOnClickListener(onLocationButtonClickListener);
        // set the default location
        setLocationById(R.id.otherImageButton);

        Button settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(onSettingsButtonClickListener);

        myHelpTextView = (TextView) findViewById(R.id.helpTextView);
        myHelpTextView.setOnClickListener(onHelpTextViewClickListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            // Kill the app from back on Mood Entry, it is topmost.
            // Needed because a notification open can cause back to point to
            // weird places. - STC 12/27/11
            this.finish();

            return true;
        }
        return false;
    };

    // click handler for location button on the bottom - STC 10/17/11
    OnClickListener onLocationButtonClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            int viewId = v.getId();
            setLocationById(viewId);

            mUserClickedLocation = true;
        }
    };

    // click handler for location button on the bottom - STC 10/17/11
    OnClickListener onSettingsButtonClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent i = new Intent(HomeActivity.this, MoodMapPreference.class);
            startActivity(i);
        }
    };

    // click handler for helpTextView - STC 12/13/11
    OnClickListener onHelpTextViewClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String helpText = "";

            long pendingAlarmTime = spIDs.getLong(Constants.PrefKey_PendingAlarmTime, 0);
            if (pendingAlarmTime > 0) {
                Date pendingAlarm = new Date(pendingAlarmTime);
                helpText = pendingAlarm.toString();
            }

            myHelpTextView.setText(helpText);
        }
    };

    public void setLocationById(int byId)
    {
        int useColor = 2; // match MoodEntryCustumView::getColorId

        myLocButtonWork.setBackgroundColor(kLocationNotSelectedColor);
        myLocButtonHome.setBackgroundColor(kLocationNotSelectedColor);
        myLocButtonOnTheGo.setBackgroundColor(kLocationNotSelectedColor);
        myLocButtonOther.setBackgroundColor(kLocationNotSelectedColor);

        switch (byId)
        {
        // Must match old IAmAtActivity values...
            case R.id.workImageButton:
                myLocButtonWork.setBackgroundColor(kLocationSelectedColor);
                strIAmAt = Constants.kWorkLocationStr;
                mLocationType = Constants.kWorkLocation;
                useColor = 1;
                break;

            case R.id.homeImageButton:
                myLocButtonHome.setBackgroundColor(kLocationSelectedColor);
                strIAmAt = Constants.kHomeLocationStr;
                mLocationType = Constants.kHomeLocation;
                useColor = 4;
                break;

            case R.id.onGoImageButton:
                myLocButtonOnTheGo.setBackgroundColor(kLocationSelectedColor);
                strIAmAt = Constants.kOnTheGoLocationStr; // "On the go";
                mLocationType = Constants.kOnTheGoLocation;
                useColor = 3;
                break;

            case R.id.otherImageButton:
                myLocButtonOther.setBackgroundColor(kLocationSelectedColor);
                strIAmAt = Constants.kOtherLocationStr;
                mLocationType = Constants.kOtherLocation;
                useColor = 2;
                break;
        }

        // Set the color to the mood points
        cv.setPointsColor(useColor);
        cv.invalidate();
    }

    static public int getButtonIdFromLocationType(int locationType)
    {
        int buttonId;

        switch (locationType)
        {
            case Constants.kHomeLocation:
                buttonId = R.id.homeImageButton;
                break;

            case Constants.kWorkLocation:
                buttonId = R.id.workImageButton;
                break;

            case Constants.kOnTheGoLocation:
                buttonId = R.id.onGoImageButton;
                break;

            case Constants.kOtherLocation:
            default:
                buttonId = R.id.otherImageButton;
                break;
        }

        return (buttonId);
    }

    static public int getLocationTypeFromString(String locationStr)
    {
        int locationType = Constants.kUnknownLocation;

        if (locationStr.contentEquals(Constants.kOtherLocationStr)) {
            locationType = Constants.kOtherLocation;
        }
        else if (locationStr.contentEquals(Constants.kHomeLocationStr)) {
            locationType = Constants.kHomeLocation;
        }
        else if (locationStr.contentEquals(Constants.kWorkLocationStr)) {
            locationType = Constants.kWorkLocation;
        }
        else if (locationStr.contentEquals(Constants.kOnTheGoLocationStr)) {
            locationType = Constants.kOnTheGoLocation;
        }

        return (locationType);
    }

    OnTouchListener cvOncliClickListener = new OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                // is first mood selected
                if (event.getX() - 25 * scale < moodFirst.X
                        && event.getX() + 25 * scale > moodFirst.X
                        && event.getY() - 25 * scale < moodFirst.Y
                        && event.getY() + 25 * scale > moodFirst.Y)
                {
                    Log.v("First Mood", "Selected");
                    moodFirst.isSelected = true;
                    moodSecond.isSelected = false;
                }

                // is second mood selected
                else if (event.getX() - (25 * scale) < moodSecond.X
                        && event.getX() + (25 * scale) > moodSecond.X
                        && event.getY() - (25 * scale) < moodSecond.Y
                        && event.getY() + (25 * scale) > moodSecond.Y)
                {
                    Log.v("Second Mood", "Selected");
                    moodFirst.isSelected = false;
                    moodSecond.isSelected = true;
                }
                // else no mood is selected
                else
                {
                    Log.v("No Mood", "Selected");
                    moodFirst.isSelected = false;
                    moodSecond.isSelected = false;
                }

                /** Choice first or secont mood creating */
                if (!moodFirst.isCreated && moodSecond.isSelected == false)
                {
                    moodFirst.X = event.getX();
                    moodFirst.Y = event.getY();
                    moodFirst.isCreated = true;
                    moodFirst.isSelected = true;
                    setFirstMood(moodFirst);
                }
                else if (!moodSecond.isCreated && moodFirst.isSelected == false)
                {

                    moodSecond.X = event.getX();
                    moodSecond.Y = event.getY();
                    moodSecond.isCreated = true;
                    moodSecond.isSelected = true;
                    setSecondMood(moodSecond);
                }
                showOrHideArrowButton();

            }

            if (event.getAction() == MotionEvent.ACTION_UP)
            {

                float X, Y;
                X = event.getX();
                Y = event.getY();

                float viewWidth = cv.getWidth();
                float rectSize = viewWidth / 23;
                // float rectSize = viewWidth/23;
                Log.v("X* *,Y***", "==" + X + " , " + Y);

                if (X < ((viewWidth / 2) - 10 * scale) && (Y < (viewWidth / 2)))
                {
                    tapX = (float) ((Math.floor((viewWidth / 2) - X) / rectSize) * -1);// X
                                                                                       // should
                                                                                       // (-ve)
                                                                                       // in
                                                                                       // first
                                                                                       // quadrant
                    tapY = (float) (Math.floor((viewWidth / 2) - Y) / rectSize);
                    Log.v("Q", "1");
                }
                if ((X > (viewWidth / 2)) && (Y < (viewWidth / 2)))
                {
                    X = X - (viewWidth / 2);

                    /*
                     * tapX =(float)(Math.floor((X/rectSize))); tapY =
                     * (float)(Math.floor(((viewWidth/2)-Y)/rectSize)) ;
                     */
                    tapX = (X / rectSize);
                    tapY = ((viewWidth / 2) - Y) / rectSize;
                    Log.v("Q", "2");
                }
                if ((X > (viewWidth / 2)) && (Y > (viewWidth / 2)))
                {
                    X = X - (viewWidth / 2);
                    Y = Y - (viewWidth / 2);
                    /*
                     * tapX =(float)(Math.floor((X/rectSize))); tapY
                     * =(float)((Math.floor((Y/rectSize)))*-1);//Y should (-ve)
                     * in third quadrant
                     */
                    tapX = (X / rectSize);
                    tapY = (Y / rectSize) * (-1);// Y should (-ve) in third
                                                 // quadrant

                    Log.v("Q", "3");
                }
                if ((X < (viewWidth / 2)) && (Y > (viewWidth / 2)))
                {
                    /*
                     * tapX =(float)(Math.floor(((X-(viewWidth/2))/rectSize)));
                     * tapY =(float)(Math.floor(((viewWidth/2)-Y)/rectSize));
                     */
                    tapX = (X - (viewWidth / 2)) / rectSize;
                    tapY = ((viewWidth / 2) - Y) / rectSize;
                    Log.v("Q", "4");
                }

                if (tapX > 10.0)
                    tapX = (float) 10.0;
                else if (tapX < -10.0)
                    tapX = (float) -10.0;

                if (tapY > 10.0)
                    tapY = (float) 10.0;
                else if (tapY < -10.0)
                    tapY = (float) -10.0;

                Log.v("TAPX,TAPY", "" + tapX + " , " + tapY);

                DecimalFormat df = new DecimalFormat("#.#");
                String tapx = df.format(tapX);
                String tapy = df.format(tapY);
                tap_X = Float.parseFloat(tapx);
                tap_Y = Float.parseFloat(tapy);
                if (moodFirst.isSelected)
                {
                    moodFirst.X = event.getX();
                    moodFirst.Y = event.getY();
                    moodFirst.moodLevel = tap_X;
                    moodFirst.energyLevel = tap_Y;

                }
                if (moodSecond.isSelected)
                {
                    moodSecond.X = event.getX();
                    moodSecond.Y = event.getY();
                    moodSecond.moodLevel = tap_X;
                    moodSecond.energyLevel = tap_Y;
                }
                /** If mood drags */
                if (event.getX() < 10 * scale || event.getX() > cv.getWidth() - 10 * scale
                        || event.getY() < 10 * scale || event.getY() > cv.getWidth() - 10 * scale)
                {
                    if (moodFirst.isSelected)
                    {
                        moodFirst.X = -50 * scale;
                        moodFirst.Y = -50 * scale;
                        setFirstMood(moodFirst);
                    }
                    if (moodSecond.isSelected)
                    {
                        moodSecond.X = -50 * scale;
                        moodSecond.Y = -50 * scale;

                        setSecondMood(moodSecond);
                    }

                    cv.invalidate();
                    Log.v("Action", "Out of Bond");
                }
                moodFirst.isSelected = false;
                moodSecond.isSelected = false;
                showOrHideArrowButton();
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE)
            {
                Log.v("Action", "Move");
                if (moodFirst.isSelected)
                {
                    Log.v("first Mood ", "Moving");
                    moodFirst.X = event.getX() - 10 * scale;
                    moodFirst.Y = event.getY() - 10 * scale;
                    setFirstMood(moodFirst);
                }
                else if (moodSecond.isSelected)
                {
                    Log.v("Second Mood", "Moving");
                    moodSecond.X = event.getX() - 10 * scale;
                    moodSecond.Y = event.getY() - 10 * scale;
                    setSecondMood(moodSecond);
                }
                else
                {
                    Log.v("NoMood", "X=" + event.getX() + " Y=" + event.getY());
                }

                /** Delete the mood entry */
                if (event.getX() < 20 * scale || event.getX() > cv.getWidth() - 20 * scale
                        || event.getY() < 20 * scale || event.getY() > cv.getWidth() - 20 * scale)
                {
                    if (moodFirst.isSelected)
                    {
                        moodFirst.X = -20 * scale;
                        moodFirst.Y = -20 * scale;
                        moodFirst.isCreated = false;
                        setFirstMood(moodFirst);
                        isAmAtDisplay = false;
                    }
                    if (moodSecond.isSelected)
                    {
                        moodSecond.X = -20 * scale;
                        moodSecond.Y = -20 * scale;
                        moodSecond.isCreated = false;
                        setSecondMood(moodSecond);
                    }

                    cv.invalidate();
                    Log.v("Action", "Out of Bond");
                }
            }

            Log.v("TAPX formated ,TAPY", "" + tap_X + " , " + tap_Y);
            return true;
        }
    };

    void setFirstMood(Mood mood) {
        cv.setFirstMood(mood.X, mood.Y);
    }

    void setSecondMood(Mood mood) {
        cv.setSecondMood(mood.X, mood.Y);
    }

    // LocationListener - STC 12/6/11
    // Uses two location listeners:
    // coarse (quick = network)
    // fine (slow gps)
    //
    // see
    // http://devdiscoveries.wordpress.com/2010/02/04/android-use-location-services/
    //
    private void registerLocationListeners()
    {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mLocationListener_Fine == null)
        {
            mLocationListener_Fine = new LocationListener()
            {
                // LocationListener
                @Override
                public void onLocationChanged(Location location) {

                    float currentLocationAccuracy = location.getAccuracy();

                    myLocationFixCnt_Fine++;
                    if ((myLocationFixCnt_Fine >= Constants.kMaxGpsFixCnt) ||
                            ((location.hasAccuracy()) && (currentLocationAccuracy <= 60.0))) // tighter,
                                                                                             // slower
                                                                                             // location
                    {
                        // stop the fine location service
                        mLocationManager.removeUpdates(this);

                        // also stop the coarse location updates, if for some
                        // reason it has not resolved yet
                        if (mLocationListener_Coarse != null) {
                            mLocationManager.removeUpdates(mLocationListener_Coarse);
                        }
                    }
                    updateMyLocation(location);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.v(Constants.LOGTAG, "Fine Provider Disabled: " + provider);
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.v(Constants.LOGTAG, "Fine Provider Enabled: " + provider);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    myStatusChangeCnt_Fine++;
                    if ((status == LocationProvider.OUT_OF_SERVICE))
                    // not sure if needed (myStatusChangeCnt_Fine >=
                    // Constants.kMaxGpsFixCnt))
                    {
                        // if cannot resolve the location, do not leave the gps
                        // running
                        mLocationManager.removeUpdates(mLocationListener_Fine);
                    }
                    Log.v(Constants.LOGTAG, "Fine Provider Status Change (OVER): " + provider
                            + " status:" + status + " myStatusChangeCnt_Fine:"
                            + myStatusChangeCnt_Fine);

                    // LocationProvider.OUT_OF_SERVICE
                }
            };
        }

        if (mLocationListener_Coarse == null)
        {
            mLocationListener_Coarse = new LocationListener()
            {
                // LocationListener
                @Override
                public void onLocationChanged(Location location) {

                    float currentLocationAccuracy = location.getAccuracy();

                    myLocationFixCnt_Coarse++;
                    if ((myLocationFixCnt_Coarse >= Constants.kMaxGpsFixCnt) ||
                            ((location.hasAccuracy()) && (currentLocationAccuracy <= 1000.0))) // quick,
                                                                                               // rough
                                                                                               // location
                    {
                        // stop the coarse location service
                        mLocationManager.removeUpdates(this);
                    }
                    updateMyLocation(location);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.v(Constants.LOGTAG, "Provider Disabled: " + provider);
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.v(Constants.LOGTAG, "Provider Enabled: " + provider);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.v(Constants.LOGTAG, "Provider Status Change: " + provider + " status:"
                            + status);
                    // LocationProvider.OUT_OF_SERVICE
                }
            };
        }

        // still in registerLocationListeners
        //
        String provider = null;
        Criteria crta = new Criteria();
        crta.setAccuracy(Criteria.ACCURACY_FINE);
        crta.setAltitudeRequired(false);
        crta.setBearingRequired(false);
        crta.setCostAllowed(false); // Indicates whether the provider is allowed
                                    // to incur monetary cost.
        // crta.setPowerRequirement(Criteria.POWER_MEDIUM); // POWER_LOW);
        provider = mLocationManager.getBestProvider(crta, true);
        // provider = LocationManager.NETWORK_PROVIDER;

        // get the last, possibly very wrong location
        currentLocation = mLocationManager.getLastKnownLocation(provider);
        // updateMyLocation(location);

        // minTime (2nd) the minimum time interval for notifications, in
        // milliseconds. This field is only used as a hint to conserve power,
        // and actual time between location updates may be greater or lesser
        // than this value.
        // minDistance (3rd)the minimum distance interval for notifications, in
        // meters
        // should be ~ 10000, 100
        // mLocationManager.requestLocationUpdates(provider, 3000, 50,
        // mLocationListener_Fine);
        mLocationManager.requestLocationUpdates(provider, 3000, 0, mLocationListener_Fine);

        // Add second quick location provider
        Criteria coarse = new Criteria();
        coarse.setAccuracy(Criteria.ACCURACY_COARSE);
        coarse.setAltitudeRequired(false);
        coarse.setBearingRequired(false);
        // coarse.setCostAllowed(false);
        // crta.setPowerRequirement(Criteria.POWER_MEDIUM); // POWER_LOW);
        String coarseProvider = mLocationManager.getBestProvider(coarse, true);
        if ((provider != null) && (!provider.contentEquals(coarseProvider))) {
            // only add coarse location resolution if DIFFERENT than the fine
            // location provider
            mLocationManager.requestLocationUpdates(coarseProvider, 1000, 1000,
                    mLocationListener_Coarse);
        }
    }

    // good ref
    // http://stackoverflow.com/questions/4210128/android-locationmanager-stop-listening-gps-stop-the-app
    //
    private void updateMyLocation(Location location) {
        String msgStr = "";
        float currentLocationAccuracy = 0.0f;

        if (location == null) {
            msgStr = "<no location>";
        } else {

            if (location.hasAccuracy()) {
                currentLocationAccuracy = location.getAccuracy();

                if (currentLocationAccuracy < myCurrentAccuracy) // used to
                                                                 // update only
                                                                 // the more
                                                                 // accurate
                                                                 // location
                                                                 // because the
                                                                 // fine/coarse
                                                                 // both running
                {
                    currentLocation = location; // <---This location is what is
                                                // stored on Save
                    myCurrentAccuracy = currentLocationAccuracy;

                    locationWasUpdated(location); // update the Location buttons

                    msgStr = "Lat:" + location.getLatitude() + " Long:" + location.getLongitude();
                    msgStr += " Provider:" + location.getProvider() + " Accuracy: "
                            + currentLocationAccuracy + "m ";
                }
            }
        }

        Log.v(Constants.LOGTAG, msgStr);
    }

    // 12/29/11
    //
    public void resetLocationWithOverrideUser(boolean clearUserLocation)
    {
        mLocationType = Constants.kUnknownLocation;
        myLocationFixCnt_Fine = 0;
        myLocationFixCnt_Coarse = 0;
        myCurrentAccuracy = 10000.0f; // used to update only the more accurate
                                      // location becuse the fine/coarse

        if (clearUserLocation)
        {
            mUserClickedLocation = false;
        }
    }

    // Update location GUI using the db
    //
    private void locationWasUpdated(Location newLocation) {

        if (mUserClickedLocation == false)
        {
            if (mLocationType == Constants.kUnknownLocation)
            {
                MoodRepository mr = new MoodRepository();
                mLocationType = mr.findMatchingLocation(newLocation);
                // mLocationType = [DataBaseManager
                // findMatchingLocation:newLocation];

                if ((mLocationType != Constants.kUnknownLocation)
                        && (mUserClickedLocation == false))
                {
                    // Do not auto select 'On the Go'
                    if (mLocationType != Constants.kOnTheGoLocation)
                    {
                        // get the button Id from the location to update the GUI
                        int locationButtonId = getButtonIdFromLocationType(mLocationType);
                        setLocationById(locationButtonId);

                        mUserClickedLocation = false;
                    }
                }
            }
        }
    }

    /**
     * This function will check the current location in the database and if
     * current location count isEqualsCountFive then it will show the
     * i_am_at_activity
     */
    /*
     * private boolean isFifthLocation() { Log.v("Check Location","1"); String
     * str=""; int moodCreatedCount=0; if(moodFirst.isCreated)
     * moodCreatedCount++; if(moodSecond.isCreated)moodCreatedCount++; try {
     * MoodRepository mr = new MoodRepository(); str =
     * mr.nearAnyLocation(currentLocation); if(!str.equals("")) { strIAmAt =str;
     * Log.v("I am at",strIAmAt); return false; } } catch (Exception e) {
     * e.printStackTrace(); } if(str.equals("")) { MoodRepository mr = new
     * MoodRepository(); if(currentLocation!=null) {
     * if(mr.getSameLocationCount(currentLocation , moodCreatedCount) >=
     * 5)//check mood point count { showIAmAtActivity(); return true; } } }
     * return false; }
     */

    private void getDeviceWidth() {
        DEVICE_DENSITY = getResources().getDisplayMetrics().density;
        DEVICE_WIDTH = getResources().getDisplayMetrics().widthPixels;
        Log.v("HomeActivity", "**Device Density=" + DEVICE_DENSITY + " " + "DEVICE_WIDTH="
                + DEVICE_WIDTH);
    }

    OnClickListener btnTrendsOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            // STC - was for IAmAtActivity
            // Intent mIntent = new
            // Intent(getBaseContext(),IAmAtActivity.class);
            // startActivityForResult(mIntent, 0);

            // with Location on main view go right to Trends
            // from onActivityResult(int, int, Intent) - STC 10/17/11
            new TaskInsertInDatabase().execute();
            new TaskUploadData().execute();
            Log.v("I am at", "**" + strIAmAt);
            // also was in IAmAtActivity

            // store now as last alarm time because not needed with this new
            // data - STC 12/20/11
            long lastAlarmTime_mSec = (new Date()).getTime(); // now in mSec
            AlarmReceiver.setLastAlarmTime(context, lastAlarmTime_mSec);

            // set next notification
            AlarmReceiver.setNotificationTimer(HomeActivity.this);

        }// HomeActivity.this.finish();
    };

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            Intent mIntent = new Intent(HomeActivity.this, TrendsActivity.class);
            startActivity(mIntent);
            finish();
        }
    };

    /**
     * Returns the current location of the device
     * 
     * @return Location
     */

    @Override
    protected void onPause() {

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener_Fine); // stop
                                                                    // location
                                                                    // resolution
                                                                    // - STC
                                                                    // 1/2/12
            mLocationManager.removeUpdates(mLocationListener_Coarse);
        }
        super.onPause();

        // HomeActivity.this.finish();
    }

    public void showOrHideArrowButton() {
        if (moodFirst.isCreated || moodSecond.isCreated)
        {
            btnTrends.setText("Save");
            btnTrends.setVisibility(View.VISIBLE);
        }
        else {
            btnTrends.setVisibility(View.GONE);
        }
    }

    void insertInDatabase() {

        MoodRepository moodRepository;
        MoodModel mood;

        if (moodFirst.isCreated)
        {
            mood = new MoodModel();
            mood.x = moodFirst.X;
            mood.y = moodFirst.Y;

            mood.moodLevel = moodFirst.moodLevel;
            mood.energyLevel = moodFirst.energyLevel;

            mood.locationName = strIAmAt;

            mood.Date = Utils.getFormatedDate(new Date());
            mood.Time = Utils.getFormatedTime(new Date());
            if (currentLocation != null)
            {
                mood.latitude = currentLocation.getLatitude();
                mood.longitude = currentLocation.getLongitude();
                // Utils.generateNoteOnSD("Locations",
                // currentLocation.getLatitude()+", "+currentLocation.getLongitude()+", "+strIAmAt);
            }
            moodRepository = new MoodRepository();
            moodRepository.insertMood(mood);
            Log.v("", "Mood First Inserted");
        }

        if (moodSecond.isCreated)
        {
            mood = new MoodModel();
            mood.x = moodSecond.X;
            mood.y = moodSecond.Y;

            mood.moodLevel = moodSecond.moodLevel;
            mood.energyLevel = moodSecond.energyLevel;

            mood.locationName = strIAmAt;

            mood.Date = Utils.getFormatedDate(new Date());
            mood.Time = Utils.getFormatedTime(new Date());
            if (currentLocation != null)
            {
                mood.latitude = currentLocation.getLatitude();
                mood.longitude = currentLocation.getLongitude();
                // Utils.generateNoteOnSD("Locations",
                // currentLocation.getLatitude()+", "+currentLocation.getLongitude()+", "+strIAmAt);
            }
            moodRepository = new MoodRepository();
            moodRepository.insertMood(mood);
            Log.v("", "Mood Second Inserted");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0 && resultCode == RESULT_OK)
        {
            strIAmAt = data.getStringExtra("at");

            // MoodRepository mr = new MoodRepository();
            // mr.updateUnknown(strIAmAt, currentLocation);

            new TaskInsertInDatabase().execute();
            new TaskUploadData().execute();

            Log.v("I am at", "**" + strIAmAt);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Uploads data to api
     */
    protected void Uploaddata()
    {
        Log.v("HomeActivity", "********************* Started");

        String data =
                "<Session>" +
                        "<Date>" + Utils.getFormatedDate(new Date()) + "</Date>" +
                        "<Time>" + Utils.getFormatedTime(new Date()) + "</Time>" +
                        "<Location>" + strIAmAt + "</Location>";

        if (moodFirst.isCreated)
        {
            String strTemp =
                    "<MoodReading>" +
                            "<QuestionID>0</QuestionID>" +
                            "<Energy>" + moodFirst.energyLevel + "</Energy>" +
                            "<Mood>" + moodFirst.moodLevel + "</Mood>" +
                            "</MoodReading>";

            data = data.concat(strTemp);
        }
        if (moodSecond.isCreated)
        {
            String strTemp =
                    "<MoodReading>" +
                            "<QuestionID>0</QuestionID>" +
                            "<Energy>" + moodSecond.energyLevel + "</Energy>" +
                            "<Mood>" + moodSecond.moodLevel + "</Mood>" +
                            "</MoodReading>";
            data = data.concat(strTemp);
        }

        data = data.concat("</Session>");
        Log.v("StrContent", data);
        try
        {
            spIDs = PreferenceManager.getDefaultSharedPreferences(this); // STC
                                                                         // 12/29/11
            // spIDs = getApplicationContext().getSharedPreferences("IDs",
            // MODE_PRIVATE);
            studyID = spIDs.getString("StudyID", "");
            participantID = spIDs.getString("participantID", "");
            deviceID = spIDs.getString("DeviceID", "");

            Log.v("studyID, participantID, deviceID", "***---" + studyID + ", " + participantID
                    + ", " + deviceID);

            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost("http://moodphoneapp.appspot.com");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

            nvps.add(new BasicNameValuePair("StudyID", studyID));
            nvps.add(new BasicNameValuePair("DeviceID", deviceID));
            nvps.add(new BasicNameValuePair("StrContent", data));

            httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            @SuppressWarnings("unused")
            HttpResponse response = httpclient.execute(httppost);

            Log.v("Data Upload", "*********************DataUploaded");
        } catch (Exception e) {
            Log.v("Data Upload", "---Data NOT Uploaded---"); // STC 12/29/11
            e.printStackTrace();
        }
    }

    /**
     * Provides currunt location
     */
    /*
     * public class CurrentLocation { } public Location getLocation() {
     * if(currentLocation!=null) {
     * Log.v("Current Location","Latitude = "+currentLocation
     * .getLatitude()+"  "+"Longitude ="+currentLocation.getLongitude()); }
     * return currentLocation; } }
     */

    /*
     * LocationManager locationManager; public void CurrentLocation (Context
     * ctx) { try { locationManager =
     * (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
     * //MyLocationListener locationListener= new MyLocationListener(); Criteria
     * criteria = new Criteria(); criteria.setAccuracy(Criteria.NO_REQUIREMENT);
     * criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
     * locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
     * 0,0,HomeActivity.this); try { currentLocation =
     * locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
     * String provider = locationManager.getBestProvider(criteria, true);
     * Log.v("GSP Provider","*****"+provider); currentLocation =
     * locationManager.getLastKnownLocation(provider); currentLocation =
     * locationManager.getLastKnownLocation(provider); } catch (Exception e) { }
     * try { if(currentLocation==null) {
     * locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
     * 0,0,HomeActivity.this); currentLocation =
     * locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
     * currentLocation =
     * locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
     * Log.v("Provider network",""+currentLocation); } } catch (Exception e) {
     * // TODO: handle exception } } catch (Exception e) { e.printStackTrace();
     * } }
     */

    private class TaskInsertInDatabase extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            btnTrends.setVisibility(View.INVISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            insertInDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent mIntent = new Intent(HomeActivity.this, TrendsActivity.class);
            startActivity(mIntent);
            finish();

            super.onPostExecute(result);
        }
    }

    private class TaskUploadData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Uploaddata();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
        }
    }

    // Menu Support - STC 10/18/11

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_settings:
                // Launch Preference activity
                Intent i = new Intent(this, MoodMapPreference.class);
                startActivity(i);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
