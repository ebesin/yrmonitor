package com.dwayne.monitor;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.dadac.testrosbridge.RCApplication;
import com.google.gson.Gson;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.dwayne.monitor.bean.Angular;
import com.dwayne.monitor.bean.Linear;
import com.dwayne.monitor.bean.Twist;
import com.dwayne.monitor.ui.BaseActivity;
import com.dwayne.monitor.view.MyRockerView;


public class ControlActivity extends BaseActivity implements View.OnClickListener {
    private Button button;
    private MyRockerView rockerView_left;
    private MyRockerView rockerView_right;

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

    private ROSBridgeClient client;

    private float linearValue;
    private float angularValue;


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0 全透明实现
            //getWindow.setStatusBarColor(Color.TRANSPARENT)
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4 全透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        current_speed.setText(String.format("%.3f", linearValue));
                        current_angle.setText(String.format("%.3f", angularValue));
                        final Linear linear = new Linear(linearValue);
                        final Angular angular = new Angular(Math.PI*(angularValue/180));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(((RCApplication) getApplication()).isConn()) {
                                    SendDataToRos("cmd_vel", new Gson().toJson(new Twist(linear, angular)));
                                }
                            }
                        }).start();
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
        setListener();
    }

    private void initView() {
        rockerView_left = findViewById(R.id.rockerview_left);
        rockerView_right = findViewById(R.id.rockerview_right);
        current_direction_left = findViewById(R.id.current_direction_left);
        current_speed = findViewById(R.id.current_speed);
        current_direction_right = findViewById(R.id.current_direction_right);
        current_angle = findViewById(R.id.current_angle);
        client = ((RCApplication) getApplication()).getRosClient();
    }

    private void SendDataToRos(String topic, String data) {
        String msg1 = "{ \"op\": \"publish\", \"topic\": \"/" + topic + "\", \"msg\": " + data +"}";
        Log.d("SendData:",msg1);
        //        String msg2 = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
        //                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0.5 + "}}}";
        client.send(msg1);
    }

    private void setListener() {

        /**
         * 左遥感距离监听
         */
        rockerView_left.setOnDistanceLevelListener(new MyRockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                speedLevel_left = (float)level/10;
                linearValue = (float) (iscenter_left * speedLevel_left * Math.sin(current_angle_left));
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
                Log.d("level", String.valueOf(speedLevel_left));
            }
        });

        /**
         * 右遥感距离监听
         */
        rockerView_right.setOnDistanceLevelListener(new MyRockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                angleLevel_right = level;
                angularValue = (float) (iscenter_right * angleLevel_right * Math.cos(current_angle_right));
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
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
                current_angle_left = Math.PI * ((360 - angle) / 180);
                linearValue = (float) (iscenter_left * speedLevel_left * Math.sin(current_angle_left));
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
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
                current_angle_right = Math.PI * ((180 - angle) / 180);
                angularValue = (float) (iscenter_right * angleLevel_right * Math.cos(current_angle_right));
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
            }

            @Override
            public void onFinish() {

            }
        });
    }

    @Override
    public void onClick(View v) {

    }
}
