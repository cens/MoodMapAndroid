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

public class Constants
{
    public static final String LOGTAG = "MoodMap";

    public static final String PrefKey_NotificationType = "pref_notification_schedule";

    public static final int kNotificationRow_Hourly = 0;
    public static final String kNotificationRow_HourlyStr = "0";
    public static final int kNotificationRow_3xDaily = 1;
    public static final String kNotificationRow_3xDailyStr = "1";
    public static final int kNotificationRow_DailyAt = 2;
    public static final String kNotificationRow_DailyAtStr = "2";
    public static final int kNotificationRow_DailyRand = 3;
    public static final String kNotificationRow_DailyRandStr = "3";
    public static final int kNotificationRow_None = 4;
    public static final String kNotificationRow_NoneStr = "4";

    public static final String PrefKey_AlarmTime = "pref_alarm_time"; // in
                                                                      // seconds
    public static final String PrefKey_AlarmTimeStr = "pref_alarm_time_str"; // hh:mm
                                                                             // TimePickerPreference
                                                                             // string
    public static final String PrefKey_PendingAlarmTime = "pending_alarm_time"; // mSec
                                                                                // because
                                                                                // cannot
                                                                                // get
                                                                                // from
                                                                                // AlarmMagager
    public static final String PrefKey_LastAlarmTime = "last_alarm_time"; // mSec
                                                                          // (iOS
                                                                          // is
                                                                          // sec)
    public static final int kAlarmTimeRange_Start = 28800; // 8am = 28800sec
    public static final int kMiddayStart = 43200; // noon = 43200sec
    public static final int kEveningStart = 64800; // 6pm = 64800sec
    public static final int kAlarmTimeRange_End = 79200; // 10pm = 79200sec
    public static final int kTooEarly = 0;
    public static final int kMorning = 1;
    public static final int kMidday = 2;
    public static final int kEvening = 3;
    public static final int kTooLate = 4;

    // Existing prefs that were in "IDs"
    public static final String PrefKey_StudyID = "StudyID";
    public static final String PrefKey_ParticipantID = "participantID";
    public static final String PrefKey_DeviceID = "DeviceID";

    // Location
    public static int kMaxGpsFixCnt = 6;
    public static double kLocationZoneMatchRange = 0.005; // +/- matching
                                                          // location range STC
                                                          // 9/20/11
    public static final int kUnknownLocation = 0;
    public static final int kWorkLocation = 1;
    public static final int kHomeLocation = 2;
    public static final int kOnTheGoLocation = 3;
    public static final int kOtherLocation = 4;
    public static final String kHomeLocationStr = "Home"; // These MUST match
                                                          // old database
                                                          // records
    public static final String kWorkLocationStr = "Work";
    public static final String kOnTheGoLocationStr = "On the go";
    public static final String kOtherLocationStr = "Other";
}
