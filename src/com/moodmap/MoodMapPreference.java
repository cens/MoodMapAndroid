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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.TimePickerPreference;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

// import android.app.TimePickerDialog;

public class MoodMapPreference extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    static final int TIME_DIALOG_ID = 0;

    SharedPreferences mySharedPrefs;
    ListPreference myNotificationTypeList;
    TimePickerPreference myTimePickerPreference;
    EditTextPreference myGroupIdEditText;
    EditTextPreference myParticipantIdEditText;
    boolean myMustResetAlarms = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        myMustResetAlarms = false;

        mySharedPrefs = getPreferenceManager().getSharedPreferences();
        mySharedPrefs.registerOnSharedPreferenceChangeListener(this);

        myNotificationTypeList = (ListPreference) getPreferenceManager().findPreference(
                Constants.PrefKey_NotificationType);
        myTimePickerPreference = (TimePickerPreference) getPreferenceManager().findPreference(
                Constants.PrefKey_AlarmTimeStr);
        updateSummaryText(mySharedPrefs, Constants.PrefKey_NotificationType);

        myGroupIdEditText = (EditTextPreference) getPreferenceManager().findPreference(
                Constants.PrefKey_StudyID);
        updateSummaryText(mySharedPrefs, Constants.PrefKey_StudyID);

        myParticipantIdEditText = (EditTextPreference) getPreferenceManager().findPreference(
                Constants.PrefKey_ParticipantID);
        updateSummaryText(mySharedPrefs, Constants.PrefKey_ParticipantID);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Update alarm if notifications setting(s) changed - STC 12/9/11
        if (myMustResetAlarms) {
            AlarmReceiver.setLastAlarmTime(this, 0);
            AlarmReceiver.setNotificationTimer(this);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Log.v(Constants.LOGTAG, "onListItemClick");
    }

    public void updateSummaryText(SharedPreferences pref, String prefKey)
    {
        String valueStr;
        String defaultStr;

        int hours;
        int minutes;
        boolean isAm;

        if (prefKey.equals(Constants.PrefKey_NotificationType))
        {
            String value = myNotificationTypeList.getValue();

            if (value.equals(Constants.kNotificationRow_DailyAtStr))
            {
                String alarmAtStr = (String) myNotificationTypeList.getEntry();
                int alarmInSeconds = pref.getInt(Constants.PrefKey_AlarmTime, -1);

                if (alarmInSeconds == -1)
                {
                    hours = 12;
                    minutes = 0;
                    isAm = false;

                    // set the default time
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Constants.PrefKey_AlarmTimeStr, "12:00");
                    editor.commit();
                }
                else {
                    hours = alarmInSeconds / 3600;
                    minutes = (alarmInSeconds - (hours * 3600)) / 60;
                    isAm = false;
                }

                if (hours == 0) {
                    hours = 12;
                    isAm = true;
                }
                else if (hours < 12) {
                    isAm = true;
                }
                else if (hours > 12) {
                    hours -= 12;
                }

                if (isAm) {
                    alarmAtStr = String.format("Once a day at %d:%02d AM", hours, minutes);
                }
                else {
                    alarmAtStr = String.format("Once a day at %d:%02d PM", hours, minutes);
                }
                myNotificationTypeList.setSummary(alarmAtStr);

                // enable setting time
                myTimePickerPreference.setEnabled(true);
            }
            else
            {
                // disable setting time
                myTimePickerPreference.setEnabled(false);

                myNotificationTypeList.setSummary(myNotificationTypeList.getEntry());
            }
        }
        else if (prefKey.equals(Constants.PrefKey_StudyID))
        {
            defaultStr = getString(R.string.group_help);
            valueStr = pref.getString(prefKey, defaultStr);
            if (valueStr.equals("0")) {
                valueStr = defaultStr;
            }
            myGroupIdEditText.setSummary(valueStr);
        }
        else if (prefKey.equals(Constants.PrefKey_ParticipantID))
        {
            defaultStr = getString(R.string.group_help);
            valueStr = pref.getString(prefKey, defaultStr);
            if (valueStr.equals("0")) {
                valueStr = defaultStr;
            }
            myParticipantIdEditText.setSummary(valueStr);
        }
    }

    // Called when Preferences change values
    //
    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String prefKey)
    {
        if (prefKey.equals(Constants.PrefKey_NotificationType))
        {
            updateSummaryText(pref, prefKey);

            myMustResetAlarms = true;
        }
        else if (prefKey.equals(Constants.PrefKey_AlarmTimeStr))
        {
            // hours: 0..23
            String alarmTime = pref.getString(Constants.PrefKey_AlarmTimeStr, "12:00");
            int hourOfDay = Integer.valueOf(alarmTime.split(":")[0]);
            int minutes = Integer.valueOf(alarmTime.split(":")[1]);
            int timeInSeconds = (hourOfDay * 3600) + (minutes * 60);

            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(Constants.PrefKey_AlarmTime, timeInSeconds);
            editor.commit();

            // update the Notification type for alarm time change
            updateSummaryText(pref, Constants.PrefKey_NotificationType);

            myMustResetAlarms = true;
        }
        else if ((prefKey.equals(Constants.PrefKey_StudyID))
                || (prefKey.equals(Constants.PrefKey_ParticipantID)))
        {
            updateSummaryText(pref, prefKey);

            SplashScreenActivity.postDeviceID(this);
        }
    }

    /*
     * private TimePickerDialog.OnTimeSetListener mTimeSetListener = new
     * TimePickerDialog.OnTimeSetListener() { public void onTimeSet(TimePicker
     * view, int hourOfDay, int minute) { int timeInSeconds = (hourOfDay * 3600)
     * + (minute * 60); SharedPreferences.Editor editor = mySharedPrefs.edit();
     * editor.putInt(Constants.PrefKey_AlarmTime, timeInSeconds);
     * editor.commit(); // Toast.makeText(ExampleApp.this,
     * "Time is="+hourOfDay+":"+minute, Toast.LENGTH_SHORT).show(); } };
     * @Override protected Dialog onCreateDialog(int id) { switch (id) { case
     * TIME_DIALOG_ID: return new TimePickerDialog(this,mTimeSetListener, 0, 0,
     * false); } return(null); }
     */
}
