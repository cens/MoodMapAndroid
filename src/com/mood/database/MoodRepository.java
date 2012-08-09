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

package com.mood.database;

import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import com.moodmap.Constants;
import com.moodmap.HomeActivity;
import com.moodmap.Utils;

import java.util.ArrayList;

public class MoodRepository extends DBAdapter {

    public static final String CREATE_TABLE_QUERY =
            "CREATE TABLE moods (" +
                    "_id INTEGER PRIMARY KEY," +
                    "x int," +
                    "y int," +
                    "XCord int," +
                    "YCord int," +
                    "moodDate date," +
                    "moodTime time," +
                    "timeZone text," +
                    "latitude DOUBLE," +
                    "longitude DOUBLE," +
                    "locationName text" +
                    ")";

    public MoodRepository() {
        super();
    }

    public void insertMood(MoodModel mood)
    {
        String insertQuery = "INSERT INTO moods ("
                + "  x, y, XCord, YCord, moodDate, moodTime,timeZone, latitude, longitude, locationName)"
                +
                " VALUES ("
                + " ?, ?, ?, ?, ?, ?, ?, ?, ?,?"
                + ")";

        Object[] args = {
                mood.x, mood.y, mood.moodLevel, mood.energyLevel, mood.Date, mood.Time,
                Utils.getTimeZone(), mood.latitude, mood.longitude, mood.locationName
        };
        this.Query(insertQuery, args);
        this.closeDatabase();

    }

    public int[] getMoodCounts() {
        int[] moodCount = {};
        String selectQuery = "select count(x) as n from moods where moodDate >= date('now' , '-30 days') group by moodDate";
        Cursor cursor = this.selectQuery(selectQuery, null);

        if (cursor != null)
            if (cursor.getCount() > 0) {
                System.out.println("MoodRepository.getMoodCounts()>>cursor.getCount()="
                        + cursor.getCount());
                moodCount = new int[cursor.getCount()];
                int i = 0;
                cursor.moveToFirst();
                do {
                    moodCount[i] = cursor.getInt(0);
                    i++;
                } while (cursor.moveToNext());
                cursor.close();
            }
        return moodCount;
    }

    public ArrayList<MoodModel> getMoods() {

        ArrayList<MoodModel> moods = new ArrayList<MoodModel>();

        // String selectQuery =
        // "select x,y,moodDate,moodTime , count(x) as n from moods where moodDate >= date('now' , '-30 days') group by moodDate";
        String selectQuery = "select X,Y,moodDate,moodTime,locationName from moods where moodDate >= date('now' , '-30 days')";

        Cursor cursor = this.selectQuery(selectQuery, null);
        if (cursor != null)
            if (cursor.getCount() > 0) {
                // System.out.println("MoodRepository.getMoods()>>cursor.getCount()= "+cursor.getCount()
                // );
                moods = new ArrayList<MoodModel>(cursor.getCount());
                cursor.moveToFirst();
                do {
                    // Log.v("Value From Cursor", "" + cursor.getString(0));
                    MoodModel mood = new MoodModel();
                    mood.moodLevel = (cursor.getFloat(cursor.getColumnIndex("x")));
                    // System.out.println("MoodRepository.getMoods()>>XCord"+mood.x);
                    mood.energyLevel = (cursor.getFloat(cursor.getColumnIndex("y")));
                    mood.Date = "" + cursor.getString(cursor.getColumnIndex("moodDate"));
                    mood.Time = "" + cursor.getString(cursor.getColumnIndex("moodTime"));
                    mood.locationName = ""
                            + cursor.getString(cursor.getColumnIndex("locationName"));

                    moods.add(mood);
                } while (cursor.moveToNext());

                cursor.close();
            }

        /*
         * for(int i=0; i<moods.size(); i++) {
         * System.out.println("TrendsActivity.getValuesFromDatabase()>>Mood.XCord="
         * +moods.get(i).XCord); }
         */

        return moods;
    }

    /*
     * public boolean isSameLocation(Location l1) { boolean
     * isMoreThanFiveTime=false; String selectQuery =
     * "select _id, latitude, longitude from moods"; Cursor cursor =
     * selectQuery(selectQuery, null); cursor.moveToFirst(); Location loc= new
     * Location("gps"); int locationCount =0; if (cursor.getCount()>0) {
     * cursor.moveToFirst(); do {
     * loc.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
     * loc.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
     * if(l1.distanceTo(loc)<=100) { locationCount++; if(locationCount>3) {
     * Log.v("isMoreThanFiveTime","YES"); isMoreThanFiveTime= true; break; }
     * Log.v("isMoreThanFiveTime","NO");
     * Log.v("locationCount","***"+locationCount); } } while
     * (cursor.moveToNext()); cursor.close(); } return isMoreThanFiveTime; }
     */

    /*
     * public int getSameLocationCount(Location l1, int moodCreatedCount){
     * Log.v("getSaneLocationCount","1"); int locationCount=0; String
     * selectQuery = "select * from moods where locationName = 'Unknown'";
     * Cursor cursor = selectQuery(selectQuery, null); cursor.moveToFirst();
     * Location loc= new Location("gps"); if (cursor.getCount()>0) {
     * cursor.moveToFirst(); do {
     * loc.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
     * loc.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
     * if(l1.distanceTo(loc)<=100) { locationCount++; if((locationCount
     * +moodCreatedCount)>=5) { Log.v("isMoreThanFiveTime","YES"); break; }
     * Log.v("isMoreThanFiveTime","NO"); } } while (cursor.moveToNext());
     * cursor.close(); } return (locationCount+moodCreatedCount); }
     *//** For Splash screen */
    /*
     * public boolean isFifthRecord() { boolean isFifthRecord=false; String
     * selectQuery = "select count(_id) from moods"; Cursor cursor =
     * selectQuery(selectQuery, null); cursor.moveToFirst(); if
     * (cursor.getCount()>0) { cursor.moveToFirst();
     * Log.v("InFifthRecord","****"+cursor.getInt(0)); if(cursor.getInt(0)>4) {
     * isFifthRecord= true; } cursor.close(); } return isFifthRecord; }
     */

    /*
     * public void updateUnknown(String strLocationame, Location loc) { //String
     * updateQuery =
     * "Update moods set locationName= ? where locationName = 'Unknown'" ;
     * Log.v("Current Location",""+loc.getLatitude()+"   "+loc.getLongitude());
     * String selectQuery = "select * from moods"; Cursor cursor =
     * selectQuery(selectQuery, null); cursor.moveToFirst(); if
     * (cursor.getCount()>0) { cursor.moveToFirst(); do{ Location l = new
     * Location("gps");
     * l.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
     * l.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
     * Log.v("In","_____________________DO While");
     * Log.v("Cursor Lat",""+cursor.
     * getDouble(cursor.getColumnIndex("latitude")));
     * Log.v("Cursor Log",""+cursor
     * .getDouble(cursor.getColumnIndex("longitude")));
     * Log.v("Distance Between Locations","***"+loc.distanceTo(l) );
     * Log.v("Location Name"
     * ,""+cursor.getString(cursor.getColumnIndex("locationName"))); if(100 >
     * loc.distanceTo(l) ) {
     * Log.v("Updating ID","**"+cursor.getString(cursor.getColumnIndex("_id")));
     * this
     * .Query("Update moods set locationName = ? where _id="+cursor.getString
     * (cursor.getColumnIndex("_id")), new Object[]{strLocationame}); }
     * }while(cursor.moveToNext()); cursor.close(); this.closeDatabase(); } }
     */

    /*
     * public String nearAnyLocation(Location loc) {
     * Log.v("nearAnyLocation","1");
     * Log.v("nearAnyLocation Current loc",""+loc.toString()); String
     * nearLocation=""; Location loc1= new Location("gps"); String
     * query="select * from moods where locationName <> 'Unknown'"; int i=0;
     * Cursor cursor = this.selectQuery(query, null); if(loc != null) { if
     * (cursor != null){ if (cursor.getCount()>0) { cursor.moveToFirst(); do {
     * loc1.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
     * loc1.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
     * Log.v("Row "+i,cursor.getString(1)+"  "+cursor.getString(2)+"  "+cursor.
     * getString(3)); if(loc1.distanceTo(loc)<=100) { nearLocation =
     * cursor.getString(cursor.getColumnIndex("locationName")); break; } } while
     * (cursor.moveToNext()); cursor.close(); } cursor.close(); } } return
     * nearLocation; }
     */
    /*
     * public void updateLocation(String loc, Location location) { String
     * updateQuery = "update locations set latitude ="+ location.getLatitude()
     * +", longitude="+ location.getLongitude()
     * +" where locationName='"+loc+"'"; Object[] args = {};
     * this.Query(updateQuery, args); }
     */
    /*
     * public void printLocationsTable() { String query="Select * from moods";
     * int i=0; Cursor cursor = this.selectQuery(query, null); if (cursor !=
     * null) if (cursor.getCount()>0) { cursor.moveToFirst(); do {
     * Log.v("Row "+i
     * ,cursor.getString(cursor.getColumnIndex("latitude"))+"  "+cursor
     * .getString
     * (cursor.getColumnIndex("longitude"))+"  "+cursor.getString(cursor
     * .getColumnIndex("locationName"))); i++; } while (cursor.moveToNext());
     * cursor.close(); } }
     */

    // findMatchingLocation
    // 12/29/11
    //
    // First hit from the latest matching record 'near' the passed in location.
    //
    public int findMatchingLocation(Location matchLocation) {

        int locationType = Constants.kUnknownLocation;
        String locationName;
        String sqlQuery;

        if ((matchLocation != null)) // &&
                                     // CLLocationCoordinate2DIsValid([matchLocation
                                     // coordinate]))
        {
            // horizontalAccuracy
            //
            sqlQuery = String
                    .format("SELECT locationName, moodDate, moodTime, latitude, longitude FROM moods WHERE (((latitude > %.4f) AND (latitude < %.4f)) AND ((longitude > %.4f) AND (longitude < %.4f))) ORDER BY moodDate DESC , moodTime DESC",
                            (matchLocation.getLatitude() - Constants.kLocationZoneMatchRange),
                            (matchLocation.getLatitude() + Constants.kLocationZoneMatchRange),
                            (matchLocation.getLongitude() - Constants.kLocationZoneMatchRange),
                            (matchLocation.getLongitude() + Constants.kLocationZoneMatchRange));

            Log.v(Constants.LOGTAG, "location query=" + sqlQuery);

            Cursor cursor = selectQuery(sqlQuery, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();

                    // locationAnswer
                    locationName = cursor.getString(cursor.getColumnIndex("locationName"));
                    locationType = HomeActivity.getLocationTypeFromString(locationName);
                    Log.v(Constants.LOGTAG, "Location: " + locationName);
                }
                else {
                    Log.v(Constants.LOGTAG, "Location - No row data");
                }

                cursor.close();
            }
            else {
                Log.v(Constants.LOGTAG, "Location - null cursor");
            }
        }

        return (locationType);
    }

}
