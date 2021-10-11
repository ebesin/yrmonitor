package com.dwayne.monitor.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dwayne.monitor.database.DataBaseUtil;

import java.util.ArrayList;
import java.util.List;

public class DeviceTypeDao implements DeviceTypeImpl{

    @Override
    public List<String> getAllTypeName() {
        SQLiteDatabase readableDatabase = DataBaseUtil.getReadableDatabase();
        if(readableDatabase!=null){
            List<String> strings= new ArrayList<>();
            Cursor query = readableDatabase.query("device_type", new String[]{"type"},
                    null, null, null, null, null);
            while (query.moveToNext()){
                strings.add(query.getString(0));
            }
            return strings;
        }
        return null;
    }

    @Override
    public String getIntentClassByName(String name) {
        SQLiteDatabase readableDatabase = DataBaseUtil.getReadableDatabase();
        if(readableDatabase!=null){
            String selection = "name = ?";
            Cursor query = readableDatabase.query("device_type", new String[]{"intent_class"},
                    selection, new String[]{name}, null, null, null);
            query.moveToNext();
            return query.getString(0);
        }
        return null;
    }
}
