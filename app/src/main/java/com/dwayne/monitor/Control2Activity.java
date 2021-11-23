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
import android.widget.TextView;

import com.dadac.testrosbridge.RCApplication;
import com.google.gson.Gson;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.dwayne.monitor.bean.MotionCom;
import com.dwayne.monitor.ui.BaseActivity;
import com.dwayne.monitor.view.MyRockerView;


public class Control2Activity extends BaseActivity implements View.OnClickListener {
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

    /**
     * 发送指令线程
     */
    private Thread send_thread;

    /**
     * 控制命令类
     */
    final MotionCom motionCom = new MotionCom();

    /**
     * 左摇杆是否在中间
     */
    boolean left_isCenter = true;
    /**
     * 左摇杆是否在前
     */
    boolean left_isUp = false;
    /**
     * 左摇杆是否在后
     */
    boolean left_isDown = false;
    /**
     * 右摇杆是否在中间
     */
    boolean right_isCenter = true;
    /**
     * 右摇杆是否在右边
     */
    boolean right_isRight = false;
    /**
     * 右摇杆是否在左边
     */
    boolean right_isLeft = false;

    /**
     * 发送线程是否应该终止
     */
    boolean isRunning = true;

    private Runnable runnable;


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
        setThread();
        send_thread.start();
        setListener();
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
        client = ((RCApplication) getApplication()).getRosClient();
    }

    public void setThread() {
        send_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
//                    SendDataToRos("motion_com", new Gson().toJson(motionCom));
                    Log.d("send:", new Gson().toJson(motionCom));
                    Log.d("testThread", this + "\t" + Thread.currentThread().getName());
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


    private void SendDataToRos(String topic, String data) {
        String msg1 = "{ \"op\": \"publish\", \"topic\": \"/" + topic + "\", \"msg\": " + data + "}";
        //        String msg2 = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
        //                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0.5 + "}}}";
        client.send(msg1);
    }

    private void setListener() {

        /**
         * 左遥感方向监听
         */
        rockerView_left.setOnShakeListener(MyRockerView.DirectionMode.DIRECTION_2_VERTICAL, new MyRockerView.OnShakeListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void direction(MyRockerView.Direction direction) {
                if (!right_isCenter)
                    return;
                switch (direction) {
                    case DIRECTION_CENTER:
                        if (!left_isCenter) {
                            Log.d("direction", "center");
                            left_isCenter = true;
                            left_isDown = false;
                            left_isUp = false;
                            iscenter_left = 0;
                            current_direction_left.setText("无");
                            current_speed.setText("0");
                            motionCom.setValues(0, 0, 0, 0, 1);
                        }
                        break;
                    case DIRECTION_UP:
                        if (!left_isUp) {
                            Log.d("direction", "up");
                            left_isUp = true;
                            left_isDown = false;
                            left_isCenter = false;
                            current_direction_left.setText("前");
                            iscenter_left = 1;
                            motionCom.setValues(1, 0, 0, 0, 0);
                        }
                        break;
                    case DIRECTION_DOWN:
                        if (!left_isDown) {
                            Log.d("direction", "down");
                            left_isDown = true;
                            left_isCenter = false;
                            left_isUp = false;
                            current_direction_left.setText("后");
                            iscenter_left = 1;
                            motionCom.setValues(0, 1, 0, 0, 0);
                        }
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
                if (!left_isCenter)
                    return;
                switch (direction) {
                    case DIRECTION_CENTER:
                        if (!right_isCenter) {
                            right_isCenter = true;
                            right_isLeft = false;
                            right_isRight = false;
                            iscenter_right = 0;
                            motionCom.setValues(0, 0, 0, 0, 1);
                            current_direction_right.setText("无");
                        }
                        break;
                    case DIRECTION_LEFT:
                        if (!right_isLeft) {
                            right_isLeft = true;
                            right_isCenter = false;
                            right_isRight = false;
                            iscenter_right = 1;
                            motionCom.setValues(0, 0, 1, 0, 0);
                            current_direction_right.setText("左");
                        }
                        break;
                    case DIRECTION_RIGHT:
                        if (!right_isRight) {
                            right_isRight = true;
                            right_isCenter = false;
                            right_isLeft = false;
                            iscenter_right = 1;
                            motionCom.setValues(0, 0, 0, 1, 0);
                            current_direction_right.setText("右");
                        }
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
