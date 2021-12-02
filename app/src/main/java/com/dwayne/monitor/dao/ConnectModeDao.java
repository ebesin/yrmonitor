package com.dwayne.monitor.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dwayne.monitor.database.DataBaseUtil;

import java.util.ArrayList;
import java.util.List;

public class ConnectModeDao implements ConnectModeImpl{
    @Override
    public List<String> getAllConnectMode() {
        SQLiteDatabase readableDatabase = DataBaseUtil.getReadableDatabase();
        if(readableDatabase!=null){
            List<String> strings= new ArrayList<>();
            Cursor query = readableDatabase.query("connect_mode", new String[]{"connectMode"},
                    null, null, null, null, null);
            while (query.moveToNext()){
                strings.add(query.getString(0));
            }
            return strings;
        }
        return null;
    }
}
