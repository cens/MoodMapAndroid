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
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utils class is the class with public static function for reuse
 * 
 * @author Chougule Shivaprasad
 * @since Apr 19, 2011
 */
public class Utils {

    public static String getFormatedTime(Date date)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String s = formatter.format(date);
        return s;
    }

    public static String getFormatedTime(String time)
    {
        String formatedTime = "";

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat df1 = new SimpleDateFormat("hh:mm:ss");
        try {
            Date d = df.parse(time);
            formatedTime = df1.format(d);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formatedTime;
    }

    public static String getFormatedDate(Date date)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        String s = formatter.format(date);
        return s;
    }

    public static String getFormatedDate(String date)
    {
        String formatedDate = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("MMM-dd");

        try {
            Date d = sdf.parse(date);
            formatedDate = sdf1.format(d);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return formatedDate;
    }

    public static String getTimeZone()
    {
        Calendar now = Calendar.getInstance();

        // get current TimeZone using getTimeZone method of Calendar class
        TimeZone timeZone = now.getTimeZone();
        return timeZone.getDisplayName();
    }

    public static boolean checkInDatabase(Location l)
    {
        boolean isSameLocation = false;

        return isSameLocation;
    }

    public static long getDateTime(String date, String time) {
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        try {
            // SimpleDateFormat dtf = new
            // SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            // Log.v("Formated Date",dtf.format(new
            // Date(df.parse(date).getTime()+tf.parse(time).getTime())));
            return df.parse(date).getTime() + tf.parse(time).getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /*
     * public static boolean isOnline(Context ctx) { ConnectivityManager cm =
     * (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
     * return cm.getActiveNetworkInfo().isConnected(); }
     */

    public static String getDeviceId(Context ctx)
    {
        final TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice;
        tmDevice = "" + tm.getDeviceId();

        return tmDevice;
    }

    public static boolean networkStatus(Activity ctx) {
        boolean status = false;
        int i = 0;

        // Log.e(getClass().getSimpleName(),"start of  networkStatus() fun ");
        try {
            String service = Context.CONNECTIVITY_SERVICE;
            ConnectivityManager connectivity = (ConnectivityManager) ctx.getSystemService(service);
            connectivity.setNetworkPreference(1);
            if (connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected())
            {
                status = true;
            }
            else if (connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected())
            {
                status = true;
            }

            NetworkInfo networkInfo[] = connectivity.getAllNetworkInfo();
            int cnt = networkInfo.length;
            // Log.v("Network Info count",""+cnt);

            for (i = 0; i < cnt; i++) {

                // Log.v("Network Names>>",i+". "+networkInfo[i].getTypeName());

                if (networkInfo[i].isConnected() == true) {
                    status = true;
                }
            }
        } catch (Exception ee) {
            Log.e("Error", " Error at networkStatus() :=" + ee.toString());
        }

        Log.v("networkStatus()", "***" + status);
        return status;
    }

    public static void generateNoteOnSD(String sFileName, String sBody) {
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append(System.getProperty("line.separator"));
            writer.append(sBody);
            writer.flush();
            writer.close();
            // Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
