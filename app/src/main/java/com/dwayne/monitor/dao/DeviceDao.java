package com.dwayne.monitor.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dwayne.monitor.bean.Device;
import com.dwayne.monitor.database.DataBaseUtil;

import java.util.ArrayList;
import java.util.List;

public class DeviceDao implements DeviceImpl{

    @Override
    public List<Device> getAllDevices() {
        SQLiteDatabase readableDatabase = DataBaseUtil.getReadableDatabase();
        if(readableDatabase!=null){
            List<Device> devices = new ArrayList<>();
            Cursor query = readableDatabase.query("robots", new String[]{"ID", "name", "ip", "type"},
                    null, null, null, null, null);
            while (query.moveToNext()) {
                devices.add(new Device(query.getString(0), query.getString(1), query.getString(2), query.getString(3)));
            }
            return devices;
        }
        return null;
    }


    @Override
    public Device getDeviceById() {
        return null;
    }

    @Override
    public boolean updateNameById(String id, String name) {
        return false;
    }

    @Override
    public boolean updateNameAndIpById(String id, String name, String ip) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("ip", ip);
        String selection = "ID = ?";
        SQLiteDatabase writableDatabase = DataBaseUtil.getWritableDatabase();
        if(writableDatabase!=null){
            int update = writableDatabase.update("robots", values, selection, new String[]{id});
            return update >= 1;
        }
        return false;
    }

    @Override
    public boolean updataAllInfoById(String id, String name, String ip, String type) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("ip", ip);
        values.put("type",type);
        String selection = "ID = ?";
        SQLiteDatabase writableDatabase = DataBaseUtil.getWritableDatabase();
        if(writableDatabase!=null){
            int update = writableDatabase.update("robots", values, selection, new String[]{id});
            return update >= 1;
        }
        return false;
    }


}
