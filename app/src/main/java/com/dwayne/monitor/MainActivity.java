package com.dwayne.monitor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.common.CatLoadingView;
import com.dadac.testrosbridge.RCApplication;
import com.dwayne.monitor.dao.DeviceTypeDao;
import com.jilk.ros.ROSClient;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.dwayne.monitor.adapter.DeviceAdapter;
import com.dwayne.monitor.bean.Device;
import com.dwayne.monitor.dao.DeviceDao;
import com.dwayne.monitor.database.DatabaseHelper;
import com.dwayne.monitor.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import static android.widget.CursorAdapter.FLAG_AUTO_REQUERY;


public class MainActivity extends BaseActivity {

    final String TAG = "MainActivity";

    Toolbar toolbar;

    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    List<Device> devices = new ArrayList<>();

    CatLoadingView mView;
    Intent robot1_intent;
    Intent robot2_intent;

    ROSBridgeClient client;
    String ip = "192.168.1.103";
    ;   //ros的 IP
    String port = "9090";

    Intent testIntent;


    AlertDialog change_device_info_dialog;

    Context context;
    DeviceDao deviceDao;
    DeviceTypeDao deviceTypeDao;
    EditText device_name_inputview;
    EditText ip_inputview;

    List<String> allTypeName;
    ArrayAdapter<String> arrayAdapter;
    Spinner spinner;


    DatabaseHelper databaseHelper;
    SQLiteDatabase writableDatabase;
    SQLiteDatabase readableDatabase;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mView.show(getSupportFragmentManager(), "");
                    break;
                case 2:
                    mView.dismiss();
                    startActivity(robot1_intent);
                    break;
                case 3:
                    Device device = (Device) msg.obj;
                    mView.dismiss();
                    try {
                        startActivity(new Intent(MainActivity.this, Class.forName(String.valueOf(deviceTypeDao.getIntentClassByName(device.getType())))));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        Log.d(TAG, "没有对应的类");
                        startActivity(new Intent(MainActivity.this, RosBridgeActivity.class));
                    }
                    break;
                case 4:
                    mView.dismiss();
                    startActivity(testIntent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initView();
    }

    private void initView() {
        context = this;
        databaseHelper = ((RCApplication) getApplication()).getDatabaseHelper();
        writableDatabase = databaseHelper.getWritableDatabase();
        readableDatabase = databaseHelper.getReadableDatabase();
        toolbar = findViewById(R.id.toolbar);
        setToolbar();
        mView = new CatLoadingView();
        recyclerView = findViewById(R.id.device_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        deviceAdapter = new DeviceAdapter();
        setClickListener();
        recyclerView.setAdapter(deviceAdapter);
        deviceDao = new DeviceDao();
        deviceTypeDao = new DeviceTypeDao();
        devices = deviceDao.getAllDevices();
        deviceAdapter.setDevices(devices);
        allTypeName = deviceTypeDao.getAllTypeName();
        arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, allTypeName);
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        refreshDevice();
    }

    public void setClickListener() {
        deviceAdapter.setOnItemCLickListener(new DeviceAdapter.OnItemCLickListener() {
            @Override
            public void onItemClick(final Device device, DeviceAdapter.ViewHolder holder, int position) {
                Log.d("click", "点击了--------------");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (device.getIp().equals("")) {
                            try {
                                startActivity(new Intent(MainActivity.this, Class.forName(deviceTypeDao.getIntentClassByName(device.getType()))));
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                Log.d(TAG, "没有对应的类");
                                startActivity(new Intent(MainActivity.this, RosBridgeActivity.class));
                            } catch (Exception e) {
                                Log.d(TAG, e.getMessage());
                                startActivity(new Intent(MainActivity.this, RosBridgeActivity.class));
                            }
                        } else {
//                            Message message = new Message();
//                            message.what = 1;
//                            handler.sendMessage(message);
//                            connectToRobot(device);
                            try {
                                startActivity(new Intent(MainActivity.this, Class.forName(deviceTypeDao.getIntentClassByName(device.getType()))));
                            } catch (ClassNotFoundException e) {
                                Log.d(TAG, "没有对应的类");
                                startActivity(new Intent(MainActivity.this, RosBridgeActivity.class));
                            } catch (Exception e) {
                                Log.d(TAG, e.getMessage());
                                startActivity(new Intent(MainActivity.this, RosBridgeActivity.class));
                            }
                        }
                    }
                }).start();
            }
        });

        deviceAdapter.setOnItemLongClickListener(new DeviceAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Device device, DeviceAdapter.ViewHolder holder, int position) {
                createDialog(position);
            }
        });
    }

    public void connectToRobot(final Device device) {
        this.ip = ((RCApplication) getApplication()).getIp();
        client = new ROSBridgeClient("ws://" + device.getIp() + ":" + port);
        boolean conneSucc = client.connect(new ROSClient.ConnectionStatusListener() {
            @Override
            public void onConnect() {
                client.setDebug(true);
                ((RCApplication) getApplication()).setRosClient(client);
                ((RCApplication) getApplication()).setConn(true);
                showTip("连接成功");
                Message message = new Message();
                message.what = 3;
                message.obj = device;
                handler.sendMessage(message);
                Log.d(TAG, "Connect ROS success");
            }

            @Override
            public void onDisconnect(boolean normal, String reason, int code) {
                showTip("断开连接");
                ((RCApplication) getApplication()).setConn(false);
                Log.d(TAG, "ROS disconnect");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
                ((RCApplication) getApplication()).setConn(false);
                showTip("连接失败");
                Log.d(TAG, "ROS communication error");
            }
        });
    }

    private void showTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createDialog(final int index) {
        final Device device = devices.get(index);
        if (device.getIp().equals("")) {
            showToast("该设备仅提供测试连接，请勿修改");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.change_info_layout, null);
        builder.setView(layout);
        device_name_inputview = layout.findViewById(R.id.device_name_inputview);
        ip_inputview = layout.findViewById(R.id.ip_inputview);
        spinner = layout.findViewById(R.id.spinner);
        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(getIndex(index));

        device_name_inputview.setText(device.getName());
        ip_inputview.setText(device.getIp());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(device_name_inputview.getText()) && TextUtils.isEmpty(ip_inputview.getText())) {
                    showToast("请输入完整的设备信息");
                } else {
                    String name = device_name_inputview.getText().toString();
                    String ip = ip_inputview.getText().toString();
                    String type = spinner.getSelectedItem().toString();
                    boolean b = deviceDao.updataAllInfoById(device.getId(), name, ip,type);
                    if (!b) {
                        showToast("更新设备信息失败");
                        return;
                    }
                    refreshDevice();
                    showToast("修改成功");
                    deviceAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private int getIndex(int i) {
        Device device = devices.get(i);
        for (int k = 0; k < allTypeName.size(); k++) {
            if(device.getType().equals(allTypeName.get(k))){
                return k;
            }
        }
        return -1;
    }

    public void refreshDevice() {
        devices = deviceDao.getAllDevices();
        deviceAdapter.setDevices(devices);
        deviceAdapter.notifyDataSetChanged();
    }

    /**
     * 设置toolbar样式
     */
    private void setToolbar() {
        // 设置navigation button
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu1, null));
        // 设置溢出菜单的图标
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_more, null));
        // 设置Menu
        toolbar.inflateMenu(R.menu.toolbar_menu);

        // 设置Navigation Button监听
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
            }
        });

        // 设置Menu监听
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return false;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
}