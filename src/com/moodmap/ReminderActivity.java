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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class ReminderActivity extends Activity implements OnClickListener {

    private static ReminderActivity myInstance = null;

    MyBroadcastReceiver1 mReceiver = new MyBroadcastReceiver1();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myInstance = this;

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        registerReceiver(mReceiver, intentFilter);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.remainder_dialog);

        ((Button) findViewById(R.id.btnClose)).setOnClickListener(this);
        ((Button) findViewById(R.id.btnRead)).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        Log.v("Reminder Activity", "**" + "OnStart");

        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                finish();
                break;

            case R.id.btnRead:
                // STC OLD was launching to splash screen - Intent mIntent = new
                // Intent(ReminderActivity.this, SplashScreenActivity.class);
                Intent mainIntent = new Intent(ReminderActivity.this, HomeActivity.class); // STC
                                                                                           // 10/18/11

                // Add FLAG_ACTIVITY_CLEAR_TOP - STC 10/18/11
                // if set, and the activity being launched is already running in
                // the current task,
                // then instead of launching a new instance of that activity,
                // all of the other
                // activities on top of it will be closed and this Intent will
                // be delivered
                // to the (now on top) old activity as a new Intent.
                //
                // For example, consider a task consisting of the activities: A,
                // B, C, D.
                // If D calls startActivity() with an Intent that resolves to
                // the component
                // of activity B, then C and D will be finished and B receive
                // the given Intent,
                // resulting in the stack now being: A, B.
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // If set, the activity will not be launched if it is already
                // running
                // at the top of the history stack. - STC 12/27/11
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // this activity will become the start of a new task on this
                // history stack. - STC 12/27/11
                // mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                // // API 11 - FLAG_ACTIVITY_TASK_ON_HOME);
                // mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(mainIntent);

                finish();

                break;
        }

    }

    @Override
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        registerReceiver(mReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // no unregisterReceiver(mReceiver); // STC 10/18/11
    }

    @Override
    protected void onDestroy() {
        // was already commented out??? unregisterReceiver(mReceiver);

        super.onDestroy();

        myInstance = null;
    }

    /**
     * Function to check if a ReninderActivity is running.
     */
    public static boolean isInstanceCreated() {
        return (myInstance != null);
    }

    public class MyBroadcastReceiver1 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(MyBroadcastReceiver1.class.getSimpleName(), "received broadcast");

            Log.v("Recived", "Recived Broad cast GLOBLE");

            Intent startupIntent = new Intent(context, ReminderActivity.class);
            startupIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(startupIntent);

        }

    }

}
