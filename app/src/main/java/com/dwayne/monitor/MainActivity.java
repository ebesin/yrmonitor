package com.dwayne.monitor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.common.CatLoadingView;
import com.dadac.testrosbridge.RCApplication;
import com.dwayne.monitor.adapter.DeviceAdapter;
import com.dwayne.monitor.bean.Device;
import com.dwayne.monitor.dao.ConnectModeDao;
import com.dwayne.monitor.dao.DeviceDao;
import com.dwayne.monitor.dao.DeviceTypeDao;
import com.dwayne.monitor.dao.PortDao;
import com.dwayne.monitor.enums.ConnectMode;
import com.dwayne.monitor.enums.DeviceType;
import com.dwayne.monitor.mqtt.MqttClient;
import com.dwayne.monitor.mqtt.MqttEvent;
import com.dwayne.monitor.ui.BaseActivity;
import com.jilk.ros.ROSClient;
import com.jilk.ros.rosbridge.ROSBridgeClient;

import org.angmarch.views.NiceSpinner;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import info.mqtt.android.service.MqttAndroidClient;


public class MainActivity extends BaseActivity {

    private final String TAG = "MainActivity";
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    private List<Device> devices = new ArrayList<>();
    private CatLoadingView mView;
    private Intent robot1_intent;
    private ROSBridgeClient client;
    private String ip = "192.168.1.103";
    //ros的 IP
    private String port = "9090";
    private Intent deviceActivity;
    private Bundle bundle;
    private AlertDialog change_device_info_dialog;
    private Context context;
    private DeviceDao deviceDao;
    private DeviceTypeDao deviceTypeDao;
    private ConnectModeDao connectModeDao;
    private PortDao portDao;
    private EditText device_name_inputview;
    private EditText ip_inputview;
    private EditText port_inputview;
    private List<String> allTypeName;
    private List<String> allConnectMode;
    private ArrayAdapter arrayAdapter;
    private ArrayAdapter arrayAdapter2;
    private NiceSpinner deviceTypeSpinner;
    private NiceSpinner connectModeSpinner;


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    mView.show(getSupportFragmentManager(), "");
                    break;
                case 3:
                    mView.dismiss();
                    try {
                        Bundle bundle = msg.getData();
                        Intent intent = new Intent(context, Class.forName(String.valueOf(bundle.getString("device_activity"))));
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } catch (ClassNotFoundException e) {
                        Log.d(TAG, "没有对应的类\n" + e.getCause());
                        startActivity(new Intent(MainActivity.this, RosBridgeActivity.class));
                    }
                    break;
                case 4:
                    mView.dismissAllowingStateLoss();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initView();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        context = this;
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
        connectModeDao = new ConnectModeDao();
        portDao = new PortDao();

        devices = deviceDao.getAllDevices();
        deviceAdapter.setDevices(devices);
        allTypeName = deviceTypeDao.getAllTypeName();
        allConnectMode = connectModeDao.getAllConnectMode();
        arrayAdapter = new ArrayAdapter<>(this, R.layout.item_select, allTypeName);
        arrayAdapter.setDropDownViewResource(R.layout.item_drapdown);
        arrayAdapter2 = new ArrayAdapter<>(this, R.layout.item_select, allConnectMode);
        arrayAdapter2.setDropDownViewResource(R.layout.item_drapdown);
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
                        //如果ip为空则为测试连接界面，跳转到测试连接界面
                        if (device.getIp().equals("")) {
                            try {
                                startActivity(new Intent(MainActivity.this, Class.forName(deviceTypeDao.getIntentClassByName(device.getType()))));
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                Log.d(TAG, "没有对应的类");
                                startActivity(new Intent(MainActivity.this, RosBridgeActivity.class));
                            } catch (Exception e) {
                                Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                                startActivity(new Intent(MainActivity.this, RosBridgeActivity.class));
                            }
                        } else {
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                            //如果为远程连接模式则进行mqtt消息订阅并跳转到相应界面
                            if (device.getConnectMode().equals(ConnectMode.REMOTEMODE.getMode())) {
                                subscribeTopic(device);
                            }
                            //如果为局域网连接方式则建立socket连接并跳转到界面
                            else if (device.getConnectMode().equals(ConnectMode.LANMODE.getMode())) {
                                connectToRobot(device);
                            }
                            //否则直接跳转
                            else {
                                Message msg = new Message();
                                msg.what = 3;
                                bundle = new Bundle();
                                bundle.putSerializable("connect_mode", ConnectMode.TESTMODE);
                                bundle.putString("device_activity", deviceTypeDao.getIntentClassByName(device.getType()));
                                bundle.putString("device_type","none");
                                msg.setData(bundle);
                                handler.sendMessage(msg);
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
        client = new ROSBridgeClient("ws://" + device.getIp() + ":" + device.getPort());
        boolean conneSucc = client.connect(new ROSClient.ConnectionStatusListener() {
            @Override
            public void onConnect() {
                client.setDebug(true);
                ((RCApplication) getApplication()).setRosClient(client);
                ((RCApplication) getApplication()).setConn(true);
//                String msg1 = "{\"op\":\"subscribe\",\"topic\":\"/status\"}";
                client.send("{\"op\":\"subscribe\",\"topic\":\"/status\"}");
//                String msg2 = "{\"op\":\"subscribe\",\"topic\":\"/battery\"}";
                client.send("{\"op\":\"subscribe\",\"topic\":\"/battery\"}");
//                String msg3 = "{\"op\":\"subscribe\",\"topic\":\"/control\"}";
                client.send("{\"op\":\"subscribe\",\"topic\":\"/control\"}");
//                String msg4 = "{\"op\":\"subscribe\",\"topic\":\"/pwm_control\"}";
                client.send("{\"op\":\"subscribe\",\"topic\":\"/pwm_control\"}");
                client.send("{\"op\":\"subscribe\",\"topic\":\"/mypath\"}");
                showTip("连接成功");
                Message message = new Message();
                message.what = 3;
                bundle = new Bundle();
                bundle.putSerializable("connect_mode", ConnectMode.LANMODE);
                bundle.putString("device_activity", deviceTypeDao.getIntentClassByName(device.getType()));
                bundle.putString("device_type",device.getType());
                message.setData(bundle);
                handler.sendMessage(message);
                Log.d(TAG, "Connect ROS success");
            }

            @Override
            public void onDisconnect(boolean normal, String reason, int code) {
                Log.d("code",code+"");
                if(code == 1000){
                    showTip("连接已断开");
                }
                if(code == 1006){
                    showTip("连接异常");
                }
                handler.sendEmptyMessage(4);
                ((RCApplication) getApplication()).setConn(false);
                Log.d(TAG, "ROS disconnect");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
                ((RCApplication) getApplication()).setConn(false);
                showTip("连接失败");
                Log.i(TAG, "Message:"+ex.getMessage()+"\n"+"CauseBy"+ex.getCause());
            }
        });
    }

    @Subscribe
    public void onEvent(MqttEvent mqttEvent){
        Log.d(TAG,"receive mqtt Data====>topic: "+mqttEvent.getTopic());
    }

    private void subscribeTopic(Device device) {
        MqttAndroidClient mqttAndroidClient = MqttClient.getInstance(this).getmMqttClient();
        if (device.getType().equals(DeviceType.HUNTER.getType())) {
            //连接成功后订阅主题
            Log.d(TAG, "subscribe MqttTopic==================>Hunter/*" );
            mqttAndroidClient.subscribe("/Hunter/status", 0);
            mqttAndroidClient.subscribe("/Hunter/battery", 0);
            mqttAndroidClient.subscribe("/Hunter/spray", 0);
        } else if (device.getType().equals(DeviceType.OLDBUNKER.getType())) {
            mqttAndroidClient.subscribe("/OldBunker/status", 0);
            mqttAndroidClient.subscribe("/OldBunker/battery", 0);
            mqttAndroidClient.subscribe("/OldBunker/spray", 0);
        } else {
            mqttAndroidClient.subscribe("/NewBunker/status", 0);
            mqttAndroidClient.subscribe("/NewBunker/battery", 0);
            mqttAndroidClient.subscribe("/NewBunker/spray", 0);
        }
        Message message = new Message();
        message.what = 3;
        bundle = new Bundle();
        bundle.putSerializable("connect_mode", ConnectMode.REMOTEMODE);
        bundle.putString("device_activity", deviceTypeDao.getIntentClassByName(device.getType()));
        bundle.putString("device_type",device.getType());
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private void showTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //根据不同的设备创建dialog，用于修改设备信息
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
        port_inputview = layout.findViewById(R.id.port_inputview);
        deviceTypeSpinner = layout.findViewById(R.id.device_type_spinner);
        deviceTypeSpinner.attachDataSource(deviceTypeDao.getAllTypeName());
        deviceTypeSpinner.setSelectedIndex(getDeviceTypeIndex(index));
        connectModeSpinner = layout.findViewById(R.id.connect_mode_spinner);
        connectModeSpinner.attachDataSource(connectModeDao.getAllConnectMode());
        connectModeSpinner.setSelectedIndex(getConnectModeIndex(index));

        device_name_inputview.setText(device.getName());
        ip_inputview.setText(device.getIp());
        port_inputview.setText(device.getPort());

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(device_name_inputview.getText()) && TextUtils.isEmpty(ip_inputview.getText())) {
                    showToast("请输入完整的设备信息");
                } else {
                    String name = device_name_inputview.getText().toString();
                    String ip = ip_inputview.getText().toString();
                    String type = deviceTypeSpinner.getSelectedItem().toString();
                    String connectMode = connectModeSpinner.getSelectedItem().toString();
                    String port = port_inputview.getText().toString();
                    boolean b = deviceDao.updataAllInfoById(device.getId(), name, ip, type, connectMode,port);
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


    private int getDeviceTypeIndex(int i) {
        Device device = devices.get(i);
        for (int k = 0; k < allTypeName.size(); k++) {
            if (device.getType().equals(allTypeName.get(k))) {
                return k;
            }
        }
        return -1;
    }

    private int getConnectModeIndex(int i) {
        Device device = devices.get(i);
        for (int k = 0; k < allConnectMode.size(); k++) {
            if (device.getConnectMode().equals(allConnectMode.get(k))) {
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