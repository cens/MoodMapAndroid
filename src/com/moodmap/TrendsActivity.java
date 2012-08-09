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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.mood.database.MoodModel;
import com.mood.database.MoodRepository;
import com.mood.models.MoodPoint;
import com.ui.TrendCustumView;

import java.util.ArrayList;

/**
 * TrendsActivity class is the activity which shows trends map to the user.
 * 
 * @author Chougule Shivaprasad
 * @since Apr 19, 2011
 */
public class TrendsActivity extends Activity {
    int DEVICE_WIDTH = 0;
    TrendCustumView cv;
    // ScrollCustumView scrollCv;

    boolean isPaused = false;

    int ViewWidth = 1;
    int rectWidth = 1;

    /** Database calculations */
    int daysCount = 0;
    // float[] xCoordinates={4,2,-3,-6,-5,-6,1,2};
    // float[] yCoordinates={4,2,3,6,-5,-5,-2,-1};
    float scale;

    int[] xCoordinatesRespective;
    int[] yCoordinatesRespective;
    int[] moodCountPerDay;

    ArrayList<MoodModel> moodsFromDB;
    ArrayList<MoodPoint> moodsToDisplay = new ArrayList<MoodPoint>();

    int X = 0, Y = 0;
    int counter = 0;

    public static Button btnPlayPause; // STC 1/9/12 btnLegend;
    public static Button mBackButton; // STC 1/11/12

    // RelativeLayout rlayout;

    // STC 1/9/12 Button btnLegends ;
    // STC 1/9/12 boolean isLegendVisible=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DEVICE_WIDTH = getResources().getDisplayMetrics().widthPixels;
        setContentView(R.layout.trends_layout);
        scale = this.getResources().getDisplayMetrics().density;
        getDeviceWidth();
        MoodRepository.context = this.getBaseContext();
        initUi();
        getValuesFromDatabase();
        counter = 0;
        xCoordinatesRespective = new int[8];
        yCoordinatesRespective = new int[8];

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getValuesFromDatabase() {
        MoodRepository moodRepository = new MoodRepository();
        moodsFromDB = new ArrayList<MoodModel>();
        moodsFromDB = moodRepository.getMoods();

        Log.v("MoodSize", "***" + moodsFromDB.size());

        MoodPoint m;

        for (MoodModel mood : moodsFromDB)
        {
            m = new MoodPoint();
            m.X = mood.moodLevel;
            m.Y = mood.energyLevel;
            m.Date = mood.Date;
            m.Time = mood.Time;
            m.color = getColorCode(mood);
            // Log.e("ColorCodes","**"+m.color);

            m.dateTime = Utils.getDateTime(mood.Date, mood.Time);
            moodsToDisplay.add(m);
        }

        moodCountPerDay = moodRepository.getMoodCounts();

        cv.setMoodValues(moodsToDisplay);
        cv.setRunning(true);
    }

    private int getColorCode(MoodModel mood) {
        // Log.e("Location Name","***"+(mood.locationName));

        if (mood.locationName.equalsIgnoreCase("Home"))
        {
            return 1;
        }
        else if (mood.locationName.equalsIgnoreCase("Work"))
        {
            return 2;
        }
        else if (mood.locationName.equalsIgnoreCase("On the go"))
        {
            return 3;
        }
        else if (mood.locationName.equalsIgnoreCase("Other"))
        {
            return 4;
        }

        return 0;
    }

    private void initUi() {
        Float t1 = 110 * scale, t2 = 20 * scale, t3 = 10 * scale;

        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                DEVICE_WIDTH, DEVICE_WIDTH + t1.intValue());
        android.widget.LinearLayout.LayoutParams scrollLp = new android.widget.LinearLayout.LayoutParams(
                DEVICE_WIDTH - t2.intValue(), t2.intValue());
        scrollLp.setMargins(t3.intValue(), 0, 0, 0);
        scrollLp.topMargin = t3.intValue();
        // rootLinearLayout.setLayoutParams(lp);

        btnPlayPause = (Button) findViewById(R.id.playPauseButton);
        btnPlayPause.setOnClickListener(playPauseOnClickListener);

        // rlayout =(RelativeLayout)findViewById(R.id.playPauseButtonLayout);
        // rlayout.setOnClickListener(playPauseOnClickListener);

        cv = (TrendCustumView) findViewById(R.id.animatedCustomView);
        cv.setLayoutParams(lp);
        cv.setRunning(false);
        cv.setOnTouchListener(onTuchListner);

        mBackButton = (Button) findViewById(R.id.backButton);
        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });

        /*
         * 1/9/12 always show legend btnLegends
         * =(Button)findViewById(R.id.legendLayout);
         * btnLegends.setVisibility(View.INVISIBLE); btnLegend = (Button)
         * findViewById(R.id.legendButton); btnLegend.setOnClickListener(new
         * OnClickListener() {
         * @Override public void onClick(View v) { if(!isLegendVisible) {
         * btnLegends.setVisibility(View.VISIBLE);
         * btnLegend.setBackgroundResource(R.drawable.hidelegend);
         * isLegendVisible=true; }else {
         * btnLegends.setVisibility(View.INVISIBLE);
         * btnLegend.setBackgroundResource(R.drawable.legend_grey_new);
         * isLegendVisible=false; } } });
         */

        ViewWidth = DEVICE_WIDTH - 20;
        rectWidth = ViewWidth / 20;
        Log.v("ViewWidth", " " + ViewWidth);
    }

    OnClickListener playPauseOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.v("Ontap", "TAP TAP TAP TAP");
            if (cv.isReplay)
            {
                Log.v("isReplay", "if");
                cv.setRunning(true);
                cv.Replay();
                btnPlayPause.setBackgroundResource(R.drawable.pause1);
            }
            else
            if (cv.isRunning())
            {
                cv.setRunning(false);
                btnPlayPause.setBackgroundResource(R.drawable.play1);
            }
            else
            {
                cv.setRunning(true);
                btnPlayPause.setBackgroundResource(R.drawable.pause1);
            }

        }
    };

    private void getDeviceWidth() {
        DEVICE_WIDTH = getResources().getDisplayMetrics().widthPixels;
        Log.v("HomeActivity", "**DEVICE_WIDTH=" + DEVICE_WIDTH);
    }

    OnTouchListener onTuchListner = new OnTouchListener() {
        Float mTouchX = 0f;
        boolean isSrollSelected = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    Log.v("Action", "Action Down");
                    mTouchX = event.getX();

                    if (((cv.getPossition() - 25 * scale) < mTouchX)
                            && ((cv.getPossition() + 25 * scale) > mTouchX))
                    {
                        Log.v("setRunning", "False");
                        cv.setRunning(false);
                        isSrollSelected = true;
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.v("Action", "Action Move");
                    mTouchX = event.getX();

                    if (isSrollSelected)
                    {
                        // Log.v("Drag position scale","--"+mTouchX + " "+
                        // scale);
                        // System.out.println("Moving X = "+mTouchX);
                        // scrollCv.setX(mTouchX);
                        // scrollCv.invalidate();
                        if (mTouchX < 20 * scale)
                            mTouchX = 20 * scale;

                        if (mTouchX > DEVICE_WIDTH)
                            mTouchX = (float) DEVICE_WIDTH - 1;
                        if (mTouchX > 310 * scale)
                        {
                            mTouchX = 320 * scale;
                        }

                        // Log.v("Drag position","--"+mTouchX);

                        cv.setDragingPosition(mTouchX.intValue());
                        cv.setPossition(mTouchX.intValue());
                        cv.isReplay = false;
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    Log.v("Action", "UP");
                    // scrollCv.setRunning(true);
                    if (isSrollSelected)
                    {
                        isSrollSelected = false;
                        mTouchX = event.getX();
                        cv.setPossition(mTouchX.intValue());
                        // cv.setRunning(true);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            goHome();

            return true;
        }
        return false;
    };

    // Broke out from onKeyDown for added 'Mood Map' button - STC 1/11/12
    //
    public void goHome() {
        // Set back to go back to add a new mood, it was commented out - STC
        // 12/27/11
        Intent homeIntent = new Intent(TrendsActivity.this, HomeActivity.class);

        // If set, and the activity being launched is already running in the
        // current task,
        // then instead of launching a new instance of that activity, all of the
        // other
        // activities on top of it will be closed and this Intent will be
        // delivered to the (now on top) old activity as a new Intent.
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // STC 12/27/11

        startActivity(homeIntent);

        finish();
    }

    @Override
    protected void onPause() {

        // Do not kill the app when Trends is no longer active from notification
        // alarm - STC 12/27/11
        // Added back because taking it out caused a crash from
        // Trends->notification->Open->Back
        // removing this makes Ignore work, but need API 11 -
        // FLAG_ACTIVITY_TASK_ON_HOME to fix above error
        TrendsActivity.this.finish();
        //
        // tried setting TrendsActivity to noHistory in the manifest because
        // it was crashing on back button after going to add a mood from
        // notification. - STC 12/27/11
        //
        // if(cv.isRunning()) { // without finish() above - STC 12/27/11
        // cv.setRunning(false);
        // }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.v("Trends Act", "OnDestroy");

        // Do not kill the app when Trends is no longer active - STC 12/27/11
        // android.os.Process.killProcess(android.os.Process.myPid());

        super.onDestroy();
    }
}
