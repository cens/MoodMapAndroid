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

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
    private static final String DATABASE_NAME = "Mood";
    private static final int DATABASE_VERSION = 1;
    public static Context context;
    private final DBHelper mDBHelper;
    private SQLiteDatabase db;

    public DBAdapter() {
        mDBHelper = new DBHelper(context);

    }

    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            /** Execute query to create tables */
            db.execSQL(MoodRepository.CREATE_TABLE_QUERY);

            try {

            } catch (SQLException se) {
                Log.v("DBAdapter", "OnCreateError");
                se.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }

    protected Cursor selectQuery(String sql, String[] args) {
        Cursor cursor = null;
        // String[] argv = (String[]) args;
        try {

            db = mDBHelper.getWritableDatabase();

            Log.v("Select Query", "" + sql);

            cursor = db.rawQuery(sql, args);
            Log.v("Select Query Cursor Count", "" + cursor.getCount());

            db.close();

        } catch (Exception e) {
            Log.v("DBAdapter", "selectQuery");
            e.printStackTrace();
        }

        return cursor;

    }

    protected void Query(String sql, Object[] bindArgs) {
        try {
            db = mDBHelper.getWritableDatabase();
            db.execSQL(sql, bindArgs);
            db.close();
        } catch (Exception e) {
            Log.v("DBAdapter Query", "" + sql);
            Log.v("DBAdapter", "Query Exeption");
            e.printStackTrace();
        }
    }

    public void closeDatabase() {
        db.close();

    }

    public void deleteQuery(String sql)
    {
        try {
            db = mDBHelper.getWritableDatabase();
            db.execSQL(sql);
            db.close();
        } catch (Exception e) {
            Log.v("DBAdapter", "deleteQuery");
            e.printStackTrace();
        }
    }

    public void createDatabse() {
        db = mDBHelper.getWritableDatabase();
    }

}// end DBAdapter
