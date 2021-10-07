package com.jiyouliang.monitor;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dadac.testrosbridge.RCApplication;
import com.jiyouliang.monitor.adapter.DeviceAdapter;
import com.jiyouliang.monitor.bean.Device;
import com.jiyouliang.monitor.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class Main2 extends Activity {

    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    List<Device> devices = new ArrayList<>();

    DatabaseHelper databaseHelper;
    SQLiteDatabase writableDatabase;
    SQLiteDatabase readableDatabase;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout2);
        initView();
        initData();
    }

    public void initView(){
        databaseHelper = ((RCApplication)getApplication()).getDatabaseHelper();
        writableDatabase = databaseHelper.getWritableDatabase();
        readableDatabase = databaseHelper.getReadableDatabase();
        devices.add(new Device("1","robot","1231232"));
        devices.add(new Device("1","robot","1231232"));
        recyclerView = findViewById(R.id.device_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2, GridLayoutManager.VERTICAL,false));
        deviceAdapter = new DeviceAdapter();
        deviceAdapter.setDevices(devices);
        recyclerView.setAdapter(deviceAdapter);
        deviceAdapter.notifyDataSetChanged();
    }

    public void initData(){

    }
}
