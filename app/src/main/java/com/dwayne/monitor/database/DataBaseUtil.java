package com.dwayne.monitor.database;

import android.database.sqlite.SQLiteDatabase;

public class DataBaseUtil {
    public static DatabaseHelper databaseHelper = null;
    public static void setDatabaseHelper(DatabaseHelper databaseHelper){
        DataBaseUtil.databaseHelper = databaseHelper;
    }
    public static SQLiteDatabase getWritableDatabase(){
        if(databaseHelper!=null){
            return databaseHelper.getWritableDatabase();
        }else {
            return null;
        }
    }

    public static SQLiteDatabase getReadableDatabase(){
        if(databaseHelper!=null){
            return databaseHelper.getReadableDatabase();
        }else {
            return null;
        }
    }
}
