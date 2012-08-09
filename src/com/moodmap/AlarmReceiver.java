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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {

    MediaPlayer myMedia;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Time Changed", "Tick");

        /** Set reminder */
        long nextAlarm_mSec = setNotificationTimer(context);

        /*
         * test AlertDialog.Builder builder = new AlertDialog.Builder(context);
         * builder.setMessage("Mood Map") .setCancelable(true)
         * .setPositiveButton("Open", new DialogInterface.OnClickListener() {
         * public void onClick(DialogInterface dialog, int id) { //
         * HomeActivity.this.finish(); } }) .setNegativeButton("Ignore", new
         * DialogInterface.OnClickListener() { public void
         * onClick(DialogInterface dialog, int id) { } }); AlertDialog alert =
         * builder.create(); alert.show();
         */

        if (!ReminderActivity.isInstanceCreated()) // STC 10/18/11
        {
            Intent myIntent = new Intent(context, ReminderActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myIntent);
        }
        else
        {
            Log.v("AlarmReceiver", "ReminderActivity is already showing.");
        }

        myMedia = new MediaPlayer();
        myMedia = MediaPlayer.create(context, R.raw.tone);
        myMedia.start();

        // store last alarm time - STC 12/13/11
        long lastAlarmTime_mSec = (new Date()).getTime(); // now in mSec
        AlarmReceiver.setLastAlarmTime(context, lastAlarmTime_mSec);

        /*
         * KeyguardManager mKeyGuardManager = (KeyguardManager)
         * context.getSystemService("keyguard"); KeyguardLock mLock =
         * mKeyGuardManager.newKeyguardLock("activity_classname");
         * mLock.disableKeyguard();
         */
    }

    public static void setLastAlarmTime(Context context, long lastAlarmTime_mSec)
    {
        SharedPreferences theSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = theSharedPrefs.edit();
        editor.putLong(Constants.PrefKey_LastAlarmTime, lastAlarmTime_mSec);
        editor.commit();

    }

    public static long setNotificationTimer(Context context)
    {
        /*
         * original pre 12/11 Calendar calendar = Calendar.getInstance();
         * calendar.getTime(); SimpleDateFormat sdf = new
         * SimpleDateFormat("HH"); String date2 =
         * sdf.format(calendar.getTime()); Log.v("Hours",""+date2); AlarmManager
         * alarmManager = (AlarmManager)
         * mContext.getSystemService(Context.ALARM_SERVICE);
         * if(Integer.parseInt(date2)>=22 ) {
         * calendar.add(Calendar.DAY_OF_MONTH, 1);
         * calendar.set(Calendar.HOUR_OF_DAY, 8); Intent intent = new
         * Intent(mContext, AlarmReceiver.class); PendingIntent pendingIntent =
         * PendingIntent.getBroadcast(mContext, 0, intent, 0);
         * alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis() ,
         * pendingIntent); } else{ Intent intent = new Intent(mContext,
         * AlarmReceiver.class); PendingIntent pendingIntent =
         * PendingIntent.getBroadcast(mContext, 0, intent, 0);
         * alarmManager.set(AlarmManager
         * .RTC_WAKEUP,Calendar.getInstance().getTimeInMillis()+ (1000*60*60) ,
         * pendingIntent);//60 Minutes }
         */

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        SharedPreferences theSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String notificationScheduleStr = theSharedPrefs.getString(
                Constants.PrefKey_NotificationType, Constants.kNotificationRow_HourlyStr);
        int notificationSchedule = Integer.valueOf(notificationScheduleStr);
        long alarmTime_mSec = 0;

        if (false) {
            // alarm testing every minute
            alarmManager.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()
                    + (1000 * 60), pendingIntent); // 60 seconds
        }
        else {
            Date nextAlarmDate = AlarmReceiver.getNextAlarmDateTimeWithSchedule(
                    notificationSchedule, theSharedPrefs);

            if (nextAlarmDate != null)
            {
                alarmTime_mSec = nextAlarmDate.getTime();
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime_mSec, pendingIntent);
            }
            else {
                alarmManager.cancel(pendingIntent);
                alarmTime_mSec = 0;
            }

            SharedPreferences.Editor editor = theSharedPrefs.edit();
            editor.putLong(Constants.PrefKey_PendingAlarmTime, alarmTime_mSec);
            editor.commit();

        }

        return (alarmTime_mSec);
    }

    // Just the time of day, in seconds
    //
    public static long getTimeInSeconds(Date fromDate)
    {
        long timeInSec;
        Calendar fromDateAsCalendar = new GregorianCalendar();
        fromDateAsCalendar.setTime(fromDate);

        timeInSec = ((fromDateAsCalendar.get(Calendar.HOUR_OF_DAY) * 60) + fromDateAsCalendar
                .get(Calendar.MINUTE)) * 60;

        return (timeInSec);
    }

    // Next alarm from now
    //
    public static Date getNextAlarmDateTimeWithSchedule(int notificationSchedule,
            SharedPreferences theSharedPrefs)
    {
        Date nextAlarmDate = null;
        Calendar nextAlarmCalendar = new GregorianCalendar();

        Calendar now = Calendar.getInstance();
        Calendar now_NoTime = (Calendar) now.clone();
        // Date now = new Date();
        // Date now_NoTime = null;
        int now_Time = 0;
        int alarmTimeOfDay;

        Date lastAlarm = null;
        Calendar lastAlarm_NoTime = null;
        int lastAlarm_Time;
        long lastAlarm_mSecs = 0;

        long startTime;
        boolean advanceFromNow = true;
        long hourTimeInSec;

        if (notificationSchedule != Constants.kNotificationRow_None)
        {
            // strip the time from today
            now_NoTime.set(Calendar.HOUR_OF_DAY, 0);
            now_NoTime.set(Calendar.MINUTE, 0);
            now_NoTime.set(Calendar.SECOND, 0);
            now_NoTime.set(Calendar.MILLISECOND, 0);
            now_Time = ((now.get(Calendar.HOUR_OF_DAY) * 60) + now.get(Calendar.MINUTE)) * 60;

            // get the last alarms set if from Notification (if not coming from
            // new Mood entry)
            // if (fromDate == null)
            {
                lastAlarm_mSecs = theSharedPrefs.getLong(Constants.PrefKey_LastAlarmTime, 0);
                if (lastAlarm_mSecs > 0)
                {
                    lastAlarm = new Date(lastAlarm_mSecs);
                    lastAlarm_NoTime = new GregorianCalendar();
                    lastAlarm_NoTime.setTimeInMillis(lastAlarm_mSecs);
                    // strip the time from lastAlarm
                    lastAlarm_NoTime.set(Calendar.HOUR_OF_DAY, 0);
                    lastAlarm_NoTime.set(Calendar.MINUTE, 0);
                    lastAlarm_NoTime.set(Calendar.SECOND, 0);
                    lastAlarm_NoTime.set(Calendar.MILLISECOND, 0);

                    Log.v(Constants.LOGTAG, "Last Alarm: " + lastAlarm);
                }
            }
            /*
             * else { // From new mood data entered, advance past this
             * Notification time. lastAlarm = fromDate; lastAlarm_NoTime = new
             * GregorianCalendar(); lastAlarm_NoTime.setTime(lastAlarm); //
             * strip the time from lastAlarm
             * lastAlarm_NoTime.set(Calendar.HOUR_OF_DAY);
             * lastAlarm_NoTime.set(Calendar.MINUTE);
             * lastAlarm_NoTime.set(Calendar.SECOND);
             * lastAlarm_NoTime.set(Calendar.MILLISECOND); lastAlarm_mSecs =
             * fromDate.getTime(); Log.v(Constants.LOGTAG,"  using date: " +
             * lastAlarm); }
             */
        }

        switch (notificationSchedule)
        {
            case Constants.kNotificationRow_Hourly:
                if (lastAlarm != null)
                {
                    if (lastAlarm.after(now.getTime()))
                    {
                        // only advance from last alarm when later than now
                        advanceFromNow = false;
                        nextAlarmCalendar.setTime(lastAlarm);
                        nextAlarmCalendar.add(Calendar.HOUR, 1);

                        nextAlarmDate = nextAlarmCalendar.getTime();
                    }
                }

                if (advanceFromNow)
                {
                    if (now_Time < Constants.kAlarmTimeRange_Start)
                    {
                        nextAlarmCalendar = (Calendar) now_NoTime.clone();
                        nextAlarmCalendar.add(Calendar.SECOND, Constants.kAlarmTimeRange_Start);
                    }
                    else
                    {
                        nextAlarmCalendar = (Calendar) now.clone();
                        nextAlarmCalendar.add(Calendar.HOUR, 1);
                    }
                    nextAlarmDate = nextAlarmCalendar.getTime();
                }

                // limit alarm to valid times.
                hourTimeInSec = AlarmReceiver.getTimeInSeconds(nextAlarmDate);
                if ((hourTimeInSec > Constants.kAlarmTimeRange_End)
                        || (hourTimeInSec < Constants.kAlarmTimeRange_Start))
                {
                    // advance to the start of the next day
                    nextAlarmCalendar = (Calendar) now_NoTime.clone();
                    nextAlarmCalendar.add(Calendar.DAY_OF_YEAR, 1);
                    nextAlarmCalendar.add(Calendar.SECOND, Constants.kAlarmTimeRange_Start);
                    nextAlarmDate = nextAlarmCalendar.getTime();

                    // tomorrowDate = [MoodsAppDelegate getDayLater:now_NoTime];
                    // nextAlarmDate = [tomorrowDate
                    // dateByAddingTimeInterval:kAlarmTimeRange_Start];
                }
                break;

            case Constants.kNotificationRow_3xDaily:
                // morning, midday, and evening with some degree of random built
                // in so that its not exactly on the hr
                //
                int nowTimeOfDay = getTimeOfDayFromSec(now_Time);

                switch (nowTimeOfDay)
                {
                    case Constants.kTooEarly:
                        nextAlarmDate = getRandomTimeFromDate(now_NoTime.getTime(),
                                Constants.kAlarmTimeRange_Start, Constants.kMiddayStart);
                        break;

                    case Constants.kMorning:
                        nextAlarmDate = getRandomTimeFromDate(now_NoTime.getTime(),
                                Constants.kMiddayStart, Constants.kEveningStart);
                        break;

                    case Constants.kMidday:
                        nextAlarmDate = getRandomTimeFromDate(now_NoTime.getTime(),
                                Constants.kEveningStart, Constants.kAlarmTimeRange_End);
                        break;

                    case Constants.kEvening:
                        // set for tomorrow morning
                        now_NoTime.add(Calendar.DAY_OF_YEAR, 1);
                        nextAlarmDate = getRandomTimeFromDate(now_NoTime.getTime(),
                                Constants.kAlarmTimeRange_Start, Constants.kMiddayStart);
                        break;
                }
                break;

            case Constants.kNotificationRow_DailyAt:
                alarmTimeOfDay = theSharedPrefs.getInt(Constants.PrefKey_AlarmTime, 43200); // 43200
                                                                                            // =
                                                                                            // noon
                                                                                            // seconds

                // add the daily alarm time to today without time
                now_NoTime.add(Calendar.SECOND, alarmTimeOfDay);
                nextAlarmDate = now_NoTime.getTime();

                // Make sure the alarm is after now.
                if (nextAlarmDate.before(now.getTime()))
                {
                    // set the alarm for tomorrow because it is past todays'
                    nextAlarmCalendar.setTime(nextAlarmDate);
                    nextAlarmCalendar.add(Calendar.DAY_OF_YEAR, 1);
                    nextAlarmDate = nextAlarmCalendar.getTime();
                }
                break;

            case Constants.kNotificationRow_DailyRand:
                // daily between 8am-10pm (8am = 28800sec, 10pm = 79200sec)
                //
                if (now_NoTime.equals(lastAlarm_NoTime)) {
                    // already had an alarm today
                    nextAlarmCalendar = (Calendar) now_NoTime.clone();
                    nextAlarmCalendar.add(Calendar.DAY_OF_YEAR, 1);
                    nextAlarmDate = nextAlarmCalendar.getTime();
                    nextAlarmDate = getRandomTimeFromDate(nextAlarmDate,
                            Constants.kAlarmTimeRange_Start, Constants.kAlarmTimeRange_End);
                }
                else {
                    // have NOT had an alarm today, but make sure to set it
                    // after now.
                    // getRandomTime between now-10pm, adding 60 just to not be
                    // before we are done
                    //
                    startTime = now_Time + 60;
                    if (startTime < Constants.kAlarmTimeRange_Start)
                    {
                        startTime = Constants.kAlarmTimeRange_Start;
                    }
                    nextAlarmDate = getRandomTimeFromDate(now_NoTime.getTime(), startTime,
                            Constants.kAlarmTimeRange_End);
                }
                break;
        }

        if (nextAlarmDate != null)
        {
            Log.v(Constants.LOGTAG, "Next Alarm: " + nextAlarmDate);
        }

        return (nextAlarmDate);
    }

    // random time between the passed in seconds
    //
    public static Date getRandomTimeFromDate(Date dateWithNoTime, long fromTimeSec, long toTimeSec)
    {
        Random randomGenerator = new Random();
        Date dateWithRandomTime = null;
        long dateWithRandomTime_mSec;
        long range;
        long randomTime;

        long dateWithNoTime_mSec = dateWithNoTime.getTime();

        // dateWithNoTime = [MoodsAppDelegate getDateWithoutTime:fromDate];

        range = toTimeSec - fromTimeSec;
        if (range >= 0)
        {
            randomTime = randomGenerator.nextInt((int) range);
            randomTime += fromTimeSec;
            dateWithRandomTime_mSec = dateWithNoTime_mSec + (randomTime * 1000);
            dateWithRandomTime = new Date(dateWithRandomTime_mSec);
        }
        else
        {
            Log.e(Constants.LOGTAG, "Invalid range in getRandomTimeFromDate");
        }

        return (dateWithRandomTime);
    }

    public static int getTimeOfDayFromSec(int timeOfDaySecs)
    {
        int timeOfDay;

        if (timeOfDaySecs < Constants.kAlarmTimeRange_Start) {
            timeOfDay = Constants.kTooEarly;
        }
        else if (timeOfDaySecs < Constants.kMiddayStart) {
            timeOfDay = Constants.kMorning;
        }
        else if (timeOfDaySecs < Constants.kEveningStart) {
            timeOfDay = Constants.kMidday;
        }
        else if (timeOfDaySecs < Constants.kAlarmTimeRange_End) {
            timeOfDay = Constants.kEvening;
        }
        else {
            timeOfDay = Constants.kTooLate;
        }

        return (timeOfDay);
    }

}
