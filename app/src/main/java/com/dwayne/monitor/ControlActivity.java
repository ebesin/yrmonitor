package com.dwayne.monitor;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.RequiresApi;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.dadac.testrosbridge.RCApplication;
import com.dwayne.monitor.enums.ConnectMode;
import com.dwayne.monitor.enums.DeviceType;
import com.dwayne.monitor.mqtt.MqttClient;
import com.google.gson.Gson;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.jaygoo.widget.VerticalRangeSeekBar;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.dwayne.monitor.bean.Angular;
import com.dwayne.monitor.bean.Linear;
import com.dwayne.monitor.bean.Twist;
import com.dwayne.monitor.ui.BaseActivity;
import com.dwayne.monitor.view.MyRockerView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.math.BigDecimal;
import java.util.Objects;


public class ControlActivity extends BaseActivity implements View.OnClickListener {
    String TAG = "ControlActivity";
    private Button button;
    private MyRockerView rockerView_left;
    private MyRockerView rockerView_right;
    private Switch send_switch;
    private Switch keep_move_switch;
    private VerticalRangeSeekBar speed_seekbar;

    private TextView current_direction_left;
    private TextView current_speed;

    private TextView current_direction_right;
    private TextView current_angle;

    private int iscenter_left = 0;
    private double speedLevel_left = 0;
    private double current_angle_left = 0;

    private int iscenter_right = 0;
    private double angleLevel_right = 0;
    private double current_angle_right = 0;

    private Handler handler;

    private ROSBridgeClient rosBridgeClient;
    private MqttAndroidClient mqttAndroidClient;

    private float linearValue;
    private float angularValue;

    private final Twist twist = new Twist(new Linear(0), new Angular(0));
    private final Twist constant_twist = new Twist(new Linear(0), new Angular(0));
    /**
     * 发送线程是否应该终止
     */
    boolean isRunning = true;
    /**
     * 遥控器是否发送消息
     */
    boolean isSending = false;
    /**
     * 是否以恒速行走
     */
    boolean constant_speed = false;
    Thread send_thread;
    int at_center_times = 0;

    ConnectMode connectMode;
    String deviceType;
    Bundle bundle;


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        //5.0 全透明实现
        //getWindow.setStatusBarColor(Color.TRANSPARENT)
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        current_speed.setText(String.format("%.3f", linearValue));
                        current_angle.setText(String.format("%.3f", angularValue));
                        break;
                    case 1:
//                        current_distance.setText(String.valueOf(speedLevel*Math.sin(current_angle)));
                        current_speed.setText(String.format("%.3f", iscenter_left * speedLevel_left * Math.sin(current_angle_left)));
                        break;
                    case 2:
                        current_angle.setText(String.format("%.3f", iscenter_right * angleLevel_right * Math.cos(current_angle_right)));
                        break;
                }
            }
        };
        initView();
        initData();
        initSeekBar();
        setListener();
        setThread();
        send_thread.start();
    }

    @Override
    protected void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    private void initView() {
        rockerView_left = findViewById(R.id.rockerview_left);
        rockerView_right = findViewById(R.id.rockerview_right);
        current_direction_left = findViewById(R.id.current_direction_left);
        current_speed = findViewById(R.id.current_speed);
        current_direction_right = findViewById(R.id.current_direction_right);
        current_angle = findViewById(R.id.current_angle);
        send_switch = findViewById(R.id.send_switch);
        keep_move_switch = findViewById(R.id.keep_move_switch);
        speed_seekbar = findViewById(R.id.speed_seekbar);
    }

    public void initData() {
        bundle = getIntent().getExtras();
        connectMode = (ConnectMode) Objects.requireNonNull(bundle.getSerializable("connect_mode"));
        if (connectMode.equals(ConnectMode.LANMODE)) {
            rosBridgeClient = ((RCApplication) getApplication()).getRosClient();
        }
        if (connectMode.equals(ConnectMode.REMOTEMODE)) {
            mqttAndroidClient = MqttClient.getInstance(this).getmMqttClient();
        }
        deviceType = (String) Objects.requireNonNull(bundle.get("device_type"));
        if (deviceType.equals(DeviceType.HUNTER.getType())) {
            rockerView_right.setmDistanceLevel(31);
        }
        if (deviceType.equals(DeviceType.NEWBUNKER.getType())) {
            rockerView_right.setmDistanceLevel(46);
        }
    }

    private void initSeekBar() {
        //保留2位小数
        speed_seekbar.setIndicatorTextDecimalFormat("0.00");
        //设置范围为-1.5-1.5，间隔为0.001
        speed_seekbar.setRange(-1.5f, 1.5f, 0.001f);
        //设置初始为0
        speed_seekbar.setProgress(0);
    }

    private void SendDataToRos(String topic, String data) {
        String msg1 = "{ \"op\": \"publish\", \"topic\": \"/" + topic + "\", \"msg\": " + data + "}";
        /*
                String msg2 = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                        0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0.5 + "}}}";
        */
        rosBridgeClient.send(msg1);
        Log.d(TAG, "SendDataToRosBridge:\t" + msg1);
    }

    private void SendDataToMqtt(String topic, String data) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(data.getBytes());
        try {
            mqttAndroidClient.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "SendDataToMqtt:\t" + data);
    }


    public void setThread() {
        send_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    //总开关打开
                    if (isSending) {
                        //如果以恒速行走
                        if (constant_speed) {
                            if (connectMode.equals(ConnectMode.LANMODE)) {
                                SendDataToRos("cmd_vel", new Gson().toJson(constant_twist));
                            } else if (connectMode.equals(ConnectMode.REMOTEMODE)) {
                                SendDataToMqtt("/hunter/control", new Gson().toJson(constant_twist));
                            }
                        }
                        //否则就看摇杆的
                        else {
                            if (connectMode.equals(ConnectMode.LANMODE)) {
                                SendDataToRos("cmd_vel", new Gson().toJson(twist));
                            } else if (connectMode.equals(ConnectMode.REMOTEMODE)) {
                                SendDataToMqtt("/hunter/control", new Gson().toJson(twist));
                            }
                        }
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        send_thread.setName("send_thread");
    }


    private void setListener() {

        /**
         * 左遥感距离监听
         */
        rockerView_left.setOnDistanceLevelListener(new MyRockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                speedLevel_left = (float) level / 10;
                linearValue = (float) (iscenter_left * speedLevel_left * Math.sin(current_angle_left));
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
                twist.setLinear(new Linear(linearValue));
                Log.d("level", String.valueOf(speedLevel_left));
            }
        });

        /**
         * 右遥感距离监听
         */
        rockerView_right.setOnDistanceLevelListener(new MyRockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                Log.d(TAG,"level:"+level);
                angleLevel_right = level;
                angularValue = (float) (iscenter_right * angleLevel_right * Math.cos(current_angle_right));
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
//                twist.setAngular(new Angular(Math.PI * (angularValue / 180)));
                twist.setAngular(new Angular(Math.toRadians(angularValue)));
            }
        });

        /**
         * 左遥感方向监听
         */
        rockerView_left.setOnShakeListener(MyRockerView.DirectionMode.DIRECTION_2_VERTICAL, new MyRockerView.OnShakeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void direction(MyRockerView.Direction direction) {
                switch (direction) {
                    case DIRECTION_CENTER:
                        iscenter_left = 0;
                        current_direction_left.setText("无");
                        current_speed.setText("0");
                        break;
                    case DIRECTION_UP:
                        current_direction_left.setText("前");
                        iscenter_left = 1;
                        break;
                    case DIRECTION_DOWN:
                        current_direction_left.setText("后");
                        iscenter_left = 1;
                        break;
                }
                linearValue = (float) (iscenter_left * speedLevel_left * Math.sin(current_angle_left));
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
                twist.setLinear(new Linear(linearValue));
            }

            @Override
            public void onFinish() {

            }
        });

        /**
         * 右遥感方向监听
         */
        rockerView_right.setOnShakeListener(MyRockerView.DirectionMode.DIRECTION_2_HORIZONTAL, new MyRockerView.OnShakeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void direction(MyRockerView.Direction direction) {
                switch (direction) {
                    case DIRECTION_CENTER:
                        iscenter_right = 0;
                        current_direction_right.setText("无");
                        break;
                    case DIRECTION_LEFT:
                        iscenter_right = 1;
                        current_direction_right.setText("左");
                        break;
                    case DIRECTION_RIGHT:
                        iscenter_right = 1;
                        current_direction_right.setText("右");
                        break;
                }
                angularValue = (float) (iscenter_right * angleLevel_right * Math.cos(current_angle_right));
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
                twist.setAngular(new Angular(Math.toRadians(angularValue)));
            }

            @Override
            public void onFinish() {

            }
        });

        /**
         * 左遥感角度监听
         */
        rockerView_left.setOnAngleChangeListener(new MyRockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void angle(double angle) {
                Log.d("angle", String.valueOf(angle));
                current_angle_left = Math.toRadians((360 - angle));
                linearValue = (float) (iscenter_left * speedLevel_left * Math.sin(current_angle_left));
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
                twist.setLinear(new Linear(linearValue));
            }

            @Override
            public void onFinish() {

            }
        });

        /**
         * 右遥感角度监听
         */
        rockerView_right.setOnAngleChangeListener(new MyRockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void angle(double angle) {
                current_angle_right = Math.toRadians((180 - angle));
                angularValue = (float) (iscenter_right * angleLevel_right * Math.cos(current_angle_right));
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
                twist.setAngular(new Angular(Math.toRadians(angularValue)));
            }

            @Override
            public void onFinish() {

            }
        });

        send_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isSending = b;
            }
        });

        keep_move_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                constant_speed = b;
            }
        });

        speed_seekbar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                Log.d(TAG, "leftValue:" + new BigDecimal(leftValue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                constant_twist.setLinear(new Linear(new BigDecimal(leftValue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });
    }

    @Override
    public void onClick(View v) {

    }
}
