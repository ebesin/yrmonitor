package com.dwayne.monitor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dadac.testrosbridge.RCApplication;
import com.google.gson.Gson;
import com.jilk.ros.ROSClient;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.jilk.ros.rosbridge.implementation.PublishEvent;
import com.dwayne.monitor.bean.Status;

import de.greenrobot.event.EventBus;


/**
 * @ Create by dadac on 2018/10/8.
 * @Function: 开始啪啦啪啦的进行数据的传输，需要放在service里面进行传输，比较保险，可以一直在传输数据
 * @Return:
 */
public class RosBridgeActivity extends Activity implements View.OnClickListener {

    ROSBridgeClient client;
    //IP
    String ip;
    // String ip = "192.168.10.20";     //半残废机器人的IP
    // String ip = "192.168.10.200";     //机器人的IP
    String port = "9090";
    String topic;

    boolean isSubscrible = false;
    private static int flagSubscrible = 0;
    boolean isConn = false;
    boolean conneSucc = false;


    private Button connect;
    private Button DC_Button_Subscrible;
    private Button DC_Button_Publish;
    private EditText datashow;
    private EditText ip_input_text;
    private EditText topic_input_text;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_conn_layout);
        EventBus.getDefault().register(this);

        subMenuShow();
        init();
    }

    private void init() {
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        String ip = sharedPreferences.getString("ip", "");
        ip_input_text.setText(ip);
        String topic = sharedPreferences.getString("topic", "");
        topic_input_text.setText(topic);
        editor = sharedPreferences.edit();
    }

    @Override
    protected void onDestroy() {
        if (isConn) {
            client.disconnect();
        }
        super.onDestroy();
    }

    //初始化界面的参数
    private void subMenuShow() {
        connect = findViewById(R.id.connect);
        connect.setOnClickListener(this);
        DC_Button_Subscrible = (Button) findViewById(R.id.subscrible);
        DC_Button_Subscrible.setOnClickListener(this);
        DC_Button_Publish = (Button) findViewById(R.id.publish);
        DC_Button_Publish.setOnClickListener(this);
        datashow = (EditText) findViewById(R.id.datashow);
        ip_input_text = findViewById(R.id.ip_input_text);
        topic_input_text = findViewById(R.id.topic_input_text);

    }


    /**
     * @Function: 建立连接
     * @Return:
     */
    public void onConnect(final String ip) {

        client = new ROSBridgeClient("ws://" + ip);
        conneSucc = client.connect(new ROSClient.ConnectionStatusListener() {
            @Override
            public void onConnect() {
                client.setDebug(true);
                ((RCApplication) getApplication()).setRosClient(client);
                isConn = true;
                connect.setText("点击断开连接");
                showTip("连接成功");
                ((RCApplication) getApplication()).setIp(ip);
                editor.putString("ip", ip);
                editor.commit();
                Log.d("dachen", "Connect ROS success");
            }

            @Override
            public void onDisconnect(boolean normal, String reason, int code) {
                isConn = false;
                connect.setText("点击连接");
                showTip("断开连接");
                Log.d("dachen", "ROS disconnect");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
                showTip("连接失败");
                Log.d("dachen", "ROS communication error");
            }
        });
    }

    //接收来自Ros端的数据
    private void ReceiveDataToRos(String topic) {
        editor.putString("topic", topic);
        editor.commit();
        if (isSubscrible) {
            String msg1 = "{\"op\":\"subscribe\",\"topic\":\"/" + topic + "\"}";
            client.send(msg1);
        } else if (!isSubscrible) {
            String msg2 = "{\"op\":\"unsubscribe\",\"topic\":\"/" + topic + "\"}";
            client.send(msg2);
        }
    }

    //发送数据到ROS端
    private void SendDataToRos(String topic, String data) {
        editor.putString("topic", topic);
        editor.commit();
        String msg1 = "{ \"op\": \"publish\", \"topic\": \"/" + topic + "\", \"msg\": { \"data\": \"" + data + " \" }}";
        //        String msg2 = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
        //                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0.5 + "}}}";
        client.send(msg1);
    }

    private void SendDataToRos2(String topic, String data) {
        editor.putString("topic", topic);
        editor.commit();
        String msg1 = "{ \"op\": \"publish\", \"topic\": \"/" + topic + "\", \"msg\": " + data + "}";
        //        String msg2 = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
        //                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0.5 + "}}}";
        client.send(msg1);
    }

    private void showTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RosBridgeActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onEvent(final PublishEvent event) {
        if (("/" + topic).equals(event.name)) {
            parseChatterTopic(event);
            return;
        }
        Log.d("dachen", event.msg);
    }



    private void parseChatterTopic(PublishEvent event) {
        Log.d("event2", "receive2");
        Log.i("dwayne2", event.msg);
        datashow.setText(event.msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.subscrible:
                if (isConn) {
                    if (!isSubscrible) {
                        if (TextUtils.isEmpty(topic_input_text.getText())) {
                            showTip("请输入topic");
                        } else {
                            isSubscrible = true;
                            DC_Button_Subscrible.setText("unSubscrible");
                        }
                    } else {
                        isSubscrible = false;
                        DC_Button_Subscrible.setText("Subscrible");
                    }
                    topic = String.valueOf(topic_input_text.getText());
                    ReceiveDataToRos(topic);
                }else{
                    showTip("请先连接");
                }
                break;
            case R.id.publish:
                if(isConn) {
                    if (TextUtils.isEmpty(topic_input_text.getText())) {
                        showTip("请输入topic");
                    } else {
                        topic = String.valueOf(topic_input_text.getText());
                        Status status = new Status(1.11111, 2.222222, 3.333333, 4.4444, 5.55555);
                        String json = new Gson().toJson(status);
                        String text = String.valueOf(datashow.getText());
                        SendDataToRos2(topic, text);
                    }
                }else{
                    showTip("请先连接");
                }
                break;
            case R.id.connect:
                if (!isConn) {
                    if (TextUtils.isEmpty(ip_input_text.getText())) {
                        showTip("请输入ip");
                    } else {
                        ip = String.valueOf(ip_input_text.getText());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                onConnect(ip);
                            }
                        }).start();
                    }
                } else {
                    client.disconnect();
                }
            default:
                break;
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    private class ComponentOnTouch implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.DC_Button_Publish:
                    onTouchChange("up", event.getAction());
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    private boolean Btn_LongPress = false;

    class MyThread extends Thread {
        @Override
        public void run() {
            while (Btn_LongPress) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                SendDataToRos("Start",);
            }
        }
    }

    private void onTouchChange(String methodName, int eventAction) {
        MyThread myThread = new MyThread();
        // 按下松开分别对应启动停止线程方法
        if ("up".equals(methodName)) {
            if (eventAction == MotionEvent.ACTION_DOWN) {
                myThread.start();
                Btn_LongPress = true;
            } else if (eventAction == MotionEvent.ACTION_UP) {
//                SendDataToRos("Stop");
                if (myThread != null)
                    Btn_LongPress = false;
            }
        }
    }
}

