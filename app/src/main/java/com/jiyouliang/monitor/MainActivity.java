package com.jiyouliang.monitor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.common.CatLoadingView;
import com.dadac.testrosbridge.RCApplication;
import com.jilk.ros.ROSClient;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.jiyouliang.monitor.bean.Device;
import com.jiyouliang.monitor.database.DatabaseHelper;
import com.jiyouliang.monitor.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener , View.OnLongClickListener {

    Toolbar toolbar;

    CardView agriculture_robot1;
    CardView agriculture_robot2;

    List<TextView> agriculture_robots = new ArrayList<>();
    Button testConn;
    CatLoadingView mView;
    Intent robot1_intent;
    Intent robot2_intent;

    ROSBridgeClient client;
    String ip = "192.168.1.103";;   //ros的 IP
    String port = "9090";

    Intent testIntent;
    Thread thread;

    AlertDialog change_device_info_dialog;
    String device_name;

    Context context;

    TextView robot1_name;
    TextView robot2_name;


    List<Device> devices = new ArrayList<>();

    EditText device_name_inputview;
    EditText ip_inputview;

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
                    startActivity(new Intent(MainActivity.this,device.getaClass()));
                    break;
                case 4:
                    mView.dismiss();
                    startActivity(testIntent);
                    break;
            }
        }
    };

    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            Message message1 = new Message();
            message1.what = 1;
            handler.sendMessage(message1);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message2 = new Message();
            message2.what = 2;
            handler.sendMessage(message2);
//            connectToRobot(devices.get(0));
        }
    };

    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            Message message1 = new Message();
            message1.what = 1;
            handler.sendMessage(message1);
            connectToRobot(devices.get(1));
        }
    };

    Runnable runnable3 = new Runnable() {
        @Override
        public void run() {
            Message message1 = new Message();
            message1.what = 1;
            handler.sendMessage(message1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message2 = new Message();
            message2.what = 4;
            handler.sendMessage(message2);
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
        databaseHelper = new DatabaseHelper(context);
        writableDatabase = databaseHelper.getWritableDatabase();
        readableDatabase = databaseHelper.getReadableDatabase();
        toolbar = findViewById(R.id.toolbar);
        agriculture_robot1 = findViewById(R.id.agriculture_robot1);
        agriculture_robot1.setOnClickListener(this);
        agriculture_robot1.setOnLongClickListener(this);
        agriculture_robot2 = findViewById(R.id.agriculture_robot2);
        agriculture_robot2.setOnClickListener(this);
        agriculture_robot2.setOnLongClickListener(this);
        robot1_name = findViewById(R.id.robot1_name);
        robot2_name = findViewById(R.id.robot2_name);
        agriculture_robots.add(robot1_name);
        agriculture_robots.add(robot2_name);
        testConn = findViewById(R.id.testConn);
        testConn.setOnClickListener(this);
        setToolbar();
        mView = new CatLoadingView();
        robot1_intent = new Intent(this, Robot1Activity.class);
        robot2_intent = new Intent(this, Robot2Activity.class);
        testIntent = new Intent(this, RosBridgeActivity.class);
        Cursor query = readableDatabase.query("robots", new String[]{"ID", "name","ip"},
                null, null, null, null, null);
        while(query.moveToNext()){
            devices.add(new Device(query.getString(0),query.getString(1),query.getString(2)));
        }
        robot1_name.setText(devices.get(0).getName());
        robot2_name.setText(devices.get(1).getName());
        devices.get(0).setName_textView(robot1_name);
        devices.get(0).setaClass(Robot1Activity.class);
        devices.get(1).setName_textView(robot2_name);
        devices.get(1).setaClass(Robot2Activity.class);
    }

    public void connectToRobot(final Device device) {
        this.ip =  ((RCApplication) getApplication()).getIp();

        client = new ROSBridgeClient("ws://" + device.getIp() + ":" + port);
        boolean conneSucc = client.connect(new ROSClient.ConnectionStatusListener() {
            @Override
            public void onConnect() {
                client.setDebug(true);
                ((RCApplication) getApplication()).setRosClient(client);
                ((RCApplication) getApplication()).setConn(true);
                showTip("连接成功");
                Message message2 = new Message();
                message2.what = 3;
                message2.obj = device;
                handler.sendMessage(message2);
                Log.d("dwayne", "Connect ROS success");
            }

            @Override
            public void onDisconnect(boolean normal, String reason, int code) {
                showTip("断开连接");
                ((RCApplication) getApplication()).setConn(false);
                Log.d("dwayne", "ROS disconnect");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
                ((RCApplication) getApplication()).setConn(false);
                showTip("连接失败");
                Log.d("dwayne", "ROS communication error");
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

    public void createDialog(final Device device){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.change_info_layout, null);
        builder.setView(layout);
        device_name_inputview = layout.findViewById(R.id.device_name_inputview);
        ip_inputview = layout.findViewById(R.id.ip_inputview);
        device_name_inputview.setText(device.getName());
        ip_inputview.setText(device.getIp());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(TextUtils.isEmpty(device_name_inputview.getText()) && TextUtils.isEmpty(ip_inputview.getText())){
                    showToast("请输入完整的设备信息");
                }else {
                    String name = device_name_inputview.getText().toString();
                    String ip = ip_inputview.getText().toString();
                    ContentValues values = new ContentValues();
                    values.put("name",name);
                    values.put("ip",ip);
                    String selection = "ID = ?";
                    writableDatabase.update("robots",values,selection,new String[]{device.getId()});
                    device.getName_textView().setText(name);
                    device.setName(name);
                    device.setIp(ip);
                    showToast("修改成功");
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.agriculture_robot1:
                thread = new Thread(runnable1);
                thread.start();
                break;
            case R.id.agriculture_robot2:
                thread = new Thread(runnable2);
                thread.start();
                break;
            case R.id.testConn:
                thread = new Thread(runnable3);
                thread.start();
                break;
            default:
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v == agriculture_robot1){
//            showToast("长按了");
            createDialog(devices.get(0));
        }else if(v == agriculture_robot2){
            createDialog(devices.get(1));
        }
        return true;
    }
}