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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.mood.database.DBAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SplashScreenActivity extends Activity
{
    static boolean requestGroupId = true;
    private final int SPLASH_DISPLAY_LENGHT = 3000;
    private SplashScreenActivity context;
    private SharedPreferences spLaunchCount, spTerms, spIDs;
    // STC private String participantID="", studyID="";
    private String deviceID = "";

    private final boolean isDeviceRegisterd = false;
    boolean isAgree = false;
    boolean firstLogin;
    private SharedPreferences mywindow;
    private int StoreddDay;
    private int StoredMonth;
    private int StoredYear;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen_layout);
        context = SplashScreenActivity.this;
        initDatabase();
        // STC spLaunchCount =
        // context.getSharedPreferences("ActivityLonchCount", MODE_PRIVATE);
        // STC spTerms = context.getSharedPreferences("Terms", MODE_PRIVATE);
        spIDs = PreferenceManager.getDefaultSharedPreferences(this); // STC
                                                                     // context.getSharedPreferences("IDs",
                                                                     // MODE_PRIVATE);
        spLaunchCount = spIDs; // use one preference STC 12/28/11
        spTerms = spIDs; // use one preference STC 12/28/11

        getDeviceId(this);// get Device ID

        // studyID = spIDs.getString("StudyID", "");
        // participantID = spIDs.getString("participantID", "");
        deviceID = spIDs.getString("DeviceID", "");
        isAgree = spIDs.getBoolean("isAgree", false);

        // isDeviceRegisterd = spIDs.getBoolean("isDeviceRegisterd", false);
        // STC 10/13/11 if(Utils.networkStatus(SplashScreenActivity.this))
        {
            loadNextActivity();
        }
        /*
         * STC 10/13/11 else{ AlertDialog.Builder builder = new
         * AlertDialog.Builder(SplashScreenActivity.this);
         * builder.setMessage("No Network connection") .setCancelable(false)
         * .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
         * public void onClick(DialogInterface dialog, int id) {
         * SplashScreenActivity.this.finish(); /**For Testing offline* /
         * //showNextActivity(); } }); AlertDialog alert = builder.create();
         * alert.show(); }
         */

    }

    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    /*
     * private void enterParticipantID() { Context mContext =
     * getApplicationContext(); LayoutInflater inflater = (LayoutInflater)
     * mContext.getSystemService(LAYOUT_INFLATER_SERVICE); View layout =
     * inflater.inflate(R.layout.request_ids_dialog, (ViewGroup)
     * findViewById(R.id.root)); builder = new
     * AlertDialog.Builder(SplashScreenActivity.this); builder.setView(layout);
     * builder.setCancelable(false); alertDialog = builder.create(); Button
     * btnCancle = (Button) layout.findViewById(R.id.button_cancel_group_id);
     * Button btnCountinue = (Button)
     * layout.findViewById(R.id.button_continue_group_id); //TextView tvHeading
     * =(TextView) layout.findViewById(R.id.tv_group_id); ImageView
     * imgDialogHeaving =(ImageView) layout.findViewById(R.id.imgHeading); final
     * EditText etID = (EditText)layout.findViewById(R.id.et_input_group_id);
     * etID.setHint("Anonymous"); //
     * tvHeading.setText("Please enter your Study ID");
     * imgDialogHeaving.setImageResource(R.drawable.participant_id);
     * btnCancle.setOnClickListener(new OnClickListener() {
     * @Override public void onClick(View arg0) { alertDialog.dismiss();
     * spIDs.edit().putString("participantID", "_").commit();
     * spIDs.edit().putBoolean("isDeviceRegisterd", true).commit();////set flag
     * is device registerd //isAgree(); //showNextActivity();
     * //isDeviceRegisterd(); if(networkStatus(SplashScreenActivity.this)) {
     * Post postDeviceID = new Post(); postDeviceID.execute(); } else//Show no
     * network dialog { /* STC 10/13/11 AlertDialog.Builder builder = new
     * AlertDialog.Builder(SplashScreenActivity.this);
     * builder.setMessage("No Network connection") .setCancelable(false)
     * .setPositiveButton("Ok", new DialogInterface.OnClickListener() { public
     * void onClick(DialogInterface dialog, int id) {
     * SplashScreenActivity.this.finish(); isDeviceRegisterd(); } });
     * AlertDialog alert = builder.create(); alert.show(); / }
     * alertDialog.dismiss(); } }); btnCountinue.setOnClickListener(new
     * OnClickListener() {
     * @Override public void onClick(View arg0) {
     * spIDs.edit().putBoolean("isDeviceRegisterd", true).commit();//set flag is
     * device registerd spIDs.edit().putString("participantID",
     * etID.getText().toString().trim()).commit(); alertDialog.dismiss(); //
     * if(Utils.networkStatus(SplashScreenActivity.this)) // STC 10/13/11
     * if(networkStatus(SplashScreenActivity.this)) { Post postDeviceID = new
     * Post(); postDeviceID.execute(); } /* STC 10/13/11 else//Show no network
     * dialog { AlertDialog.Builder builder = new
     * AlertDialog.Builder(SplashScreenActivity.this);
     * builder.setMessage("No Network connection") .setCancelable(false)
     * .setPositiveButton("Ok", new DialogInterface.OnClickListener() { public
     * void onClick(DialogInterface dialog, int id) {
     * //SplashScreenActivity.this.finish(); isDeviceRegisterd(); } });
     * AlertDialog alert = builder.create(); alert.show(); } /
     * alertDialog.dismiss(); } //showNextActivity(); }); alertDialog.show(); }
     */

    /*
     * private void enterStudyID() { Context mContext = getApplicationContext();
     * // Log.v("Context",""+mContext); LayoutInflater inflater =
     * (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE); View
     * layout = inflater.inflate(R.layout.request_ids_dialog, (ViewGroup)
     * findViewById(R.id.root)); builder = new
     * AlertDialog.Builder(SplashScreenActivity.this); builder.setView(layout);
     * builder.setCancelable(false); alertDialog = builder.create(); Button
     * btnCancle = (Button) layout.findViewById(R.id.button_cancel_group_id);
     * Button btnCountinue = (Button)
     * layout.findViewById(R.id.button_continue_group_id); final EditText etID =
     * (EditText)layout.findViewById(R.id.et_input_group_id);
     * etID.setHint("None"); ImageView imgDialogHeaving =(ImageView)
     * layout.findViewById(R.id.imgHeading);
     * imgDialogHeaving.setImageResource(R.drawable.enter_group_id);
     * btnCancle.setOnClickListener(new OnClickListener() {
     * @Override public void onClick(View arg0) { alertDialog.dismiss();
     * spIDs.edit().putString("StudyID", "_").commit();
     * enterParticipantID();//Show Next dialog } });
     * btnCountinue.setOnClickListener(new OnClickListener() {
     * @Override public void onClick(View arg0) {
     * if(!etID.getText().toString().trim().equals("")) {
     * spIDs.edit().putString("StudyID",
     * etID.getText().toString().trim()).commit(); alertDialog.dismiss(); } else
     * { alertDialog.dismiss(); } enterParticipantID();//Show Next dialog } });
     * alertDialog.show(); }
     */

    private void initDatabase()
    {
        DBAdapter.context = this.getBaseContext();
        DBAdapter dbAdapter = new DBAdapter();
        dbAdapter.createDatabse();
        dbAdapter.closeDatabase();
    }

    private void loadNextActivity()
    {
        new Handler().postDelayed(new Runnable()
        {

            @Override
            public void run()
            {

                // showNextActivity();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date date = new Date();
                String tempDate = dateFormat.format(date);

                String[] tempArrayforDate = tempDate.split("-");
                String y = null, m = null, da = null;
                for (int i = 0; i < tempArrayforDate.length; i++) {
                    da = tempArrayforDate[0];
                    m = tempArrayforDate[1];
                    y = tempArrayforDate[2];

                }
                Calendar calendar1 = Calendar.getInstance();
                Calendar calendar2 = Calendar.getInstance();
                int year = Integer.parseInt(y);
                int month = Integer.parseInt(m);
                int day = Integer.parseInt(da);
                calendar1.set(year, month, day);
                mywindow = getSharedPreferences("AppExpirydate",
                        Activity.MODE_PRIVATE);
                firstLogin = mywindow.getBoolean("firstLogin", true);
                if (firstLogin) {

                    mywindow = getSharedPreferences("AppExpirydate",
                            Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mywindow.edit();
                    editor.putInt("day", day);
                    editor.putInt("month", month);
                    editor.putInt("year", year);
                    editor.putBoolean("firstLogin", false);
                    editor.commit();
                    isDeviceRegisterd();

                } else {
                    /*
                     * mywindow = getSharedPreferences("AppExpirydate",
                     * Activity.MODE_PRIVATE); StoreddDay =
                     * mywindow.getInt("day", 0); StoredMonth=
                     * mywindow.getInt("month", 0); StoredYear
                     * =mywindow.getInt("year", 0); calendar2.set(StoredYear,
                     * StoredMonth, StoreddDay); long milliseconds1 =
                     * calendar1.getTimeInMillis(); long milliseconds2 =
                     * calendar2.getTimeInMillis(); //App first launch date and
                     * todays date difference if more than 30 days will not
                     * launch next activity long diff = milliseconds1 -
                     * milliseconds2; long diffDays = diff / (24 * 60 * 60 *
                     * 1000); if(diffDays<=30)
                     */
                    {
                        isDeviceRegisterd();
                    }
                    /*
                     * STC else{ //Toast.makeText(SplashScreenActivity.this,
                     * "expired",1000).show(); AlertDialog.Builder builder = new
                     * AlertDialog.Builder(SplashScreenActivity.this);
                     * builder.setTitle("Alert");
                     * builder.setMessage("Application Expired!")
                     * .setCancelable(false) .setPositiveButton("Ok", new
                     * DialogInterface.OnClickListener() { public void
                     * onClick(DialogInterface dialog, int id) { finish(); } });
                     * AlertDialog alert = builder.create(); alert.show(); }
                     */
                }

            }

        }, SPLASH_DISPLAY_LENGHT);
    }

    private void isDeviceRegisterd() {
        // spIDs =context.getSharedPreferences("IDs", MODE_PRIVATE);
        // spTerms = context.getSharedPreferences("Terms", MODE_PRIVATE);
        spTerms = PreferenceManager.getDefaultSharedPreferences(SplashScreenActivity.this); // STC
                                                                                            // 12/28/11

        /*
         * STC isDeviceRegisterd = spIDs.getBoolean("isDeviceRegisterd", false);
         * if (!isDeviceRegisterd) { enterStudyID(); } else
         */
        if (!spTerms.getBoolean("isAgree", false))
        {
            Intent mIntent = new Intent(SplashScreenActivity.this, Terms_Activity.class);
            startActivityForResult(mIntent, 0);
        }
        else {
            showNextActivity();
        }

    }

    private void showNextActivity() {
        Editor ed = spLaunchCount.edit();
        int count = spLaunchCount.getInt("count", -1);
        if (count == -1)// First time only
        {
            ed.putInt("count", 1);
            ed.commit();

            Intent mainIntent = new Intent(context, StartMoodMapping.class);
            context.startActivity(mainIntent);
            context.finish();

        }
        else if (count <= 5)// Show the rools to play
        {
            ed.putInt("count", count + 1);
            ed.commit();

            Intent mainIntent = new Intent(context, StartMoodMapping.class);
            context.startActivity(mainIntent);
            context.finish();

        }
        else// Start the home activity
        {
            Intent mainIntent = new Intent(context, HomeActivity.class);
            context.startActivity(mainIntent);
            context.finish();
        }
    }

    private void getDeviceId(Context ctx)
    {
        final TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice;
        tmDevice = "" + tm.getDeviceId();
        spIDs.edit().putString("DeviceID", tmDevice).commit();// Saves device id
                                                              // in shared
                                                              // prefrence
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0)
        {
            isDeviceRegisterd();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Made static so can be called from MoodMapPreferences - STC 10/19/11
    public static void postDeviceID(Context context) {
        try
        {
            SharedPreferences spIDs = PreferenceManager.getDefaultSharedPreferences(context); // STC
            // spIDs = context.getSharedPreferences("IDs", MODE_PRIVATE);

            String studyID = spIDs.getString("StudyID", "");
            String participantID = spIDs.getString("participantID", "");
            String deviceID = spIDs.getString("DeviceID", "");

            Log.v("studyID, participantID, deviceID", "***---" + studyID + ", " + participantID
                    + ", " + deviceID);

            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost("http://moodphoneapp.appspot.com/device/add");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

            nvps.add(new BasicNameValuePair("ParticipantID", participantID));
            nvps.add(new BasicNameValuePair("DeviceID", deviceID));
            nvps.add(new BasicNameValuePair("StudyID", studyID));

            httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            @SuppressWarnings("unused")
            HttpResponse response = httpclient.execute(httppost);

            // Save in shared preference
            spIDs.edit().putBoolean("isDeviceRegisterd", true).commit();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class Post extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute()
        {
            dialog = ProgressDialog.show(SplashScreenActivity.this, "", "Loading. Please wait...",
                    true);

            Log.v("Post", "onPreExecute");

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result)
        {
            dialog.dismiss();
            Log.v("Post", "onPostExecute");
            // showNextActivity();
            isDeviceRegisterd();
            // finish();
        };

        @Override
        protected Void doInBackground(Void... arg0)
        {
            // Register device
            postDeviceID(context);

            return null;
        }
    }

    public boolean networkStatus(Activity context) {
        boolean status = false;
        int i = 0;
        try {
            String service = Context.CONNECTIVITY_SERVICE;
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(service);
            connectivity.setNetworkPreference(1);

            NetworkInfo networkInfo[] = connectivity.getAllNetworkInfo();
            int cnt = networkInfo.length;

            for (i = 0; i < cnt; i++) {

                if (networkInfo[i].isConnected() == true) {
                    status = true;
                    Log.v("Network Provider", "" + networkInfo[i].getTypeName());
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        Log.v("networkStatus", "##########" + status);
        return status;
    }

    @Override
    protected void onDestroy() {
        Log.v("Splash Screen activity ", "***Finish()");
        super.onDestroy();
    }
}
