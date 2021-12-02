package com.dwayne.monitor.dao;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.dwayne.monitor.database.DataBaseUtil;

public class PortDao implements PortImpl{
    @Override
    public String getPortByDeviceId(String id) {
        SQLiteDatabase readableDatabase = DataBaseUtil.getReadableDatabase();
        if(readableDatabase!=null){
            String selection = "id = ?";
            Cursor query = readableDatabase.query("robots", new String[]{"id"},
                    selection, new String[]{id}, null, null, null);
            query.moveToNext();
            readableDatabase.close();
            return query.getString(0);
        }
        return null;
    }

    @Override
    public void updatePortByDeviceId(String id) {
        SQLiteDatabase writableDatabase = DataBaseUtil.getWritableDatabase();
        if(writableDatabase!=null){

        }
    }


}
