package com.dwayne.monitor;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.dadac.testrosbridge.RCApplication;
import com.dwayne.monitor.adapter.DeviceAdapter;
import com.dwayne.monitor.bean.Device;
import com.dwayne.monitor.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class Main2 extends Activity {

    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    List<Device> devices = new ArrayList<>();

    Toolbar toolbar;

    DatabaseHelper databaseHelper;
    SQLiteDatabase writableDatabase;
    SQLiteDatabase readableDatabase;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout2);
        initView();
    }

    public void initView(){
        databaseHelper = ((RCApplication)getApplication()).getDatabaseHelper();
        writableDatabase = databaseHelper.getWritableDatabase();
        readableDatabase = databaseHelper.getReadableDatabase();
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.device_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2, GridLayoutManager.VERTICAL,false));
        deviceAdapter = new DeviceAdapter();
        deviceAdapter.setDevices(devices);
        recyclerView.setAdapter(deviceAdapter);
    }

}
