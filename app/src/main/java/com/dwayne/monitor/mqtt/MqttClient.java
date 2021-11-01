package com.dwayne.monitor.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

public class MqttClient {
    private static MqttClient instance;
    private Context context;
    private OnConnectListener onConnectListener;
    private boolean isConnected = false;

    //单例模式
    public static MqttClient getInstance(Context context) {
        if (null == instance) {
            synchronized (MqttClient.class) {
                instance = new MqttClient(context);
            }
        }
        return instance;
    }

    private MqttClient(Context context) {
        this.context = context.getApplicationContext();
    }

    //声明一个MQTT客户端对象
    private MqttAndroidClient mMqttClient;
    private static final String TAG = "MqttClient";

    public void setOnConnectListener(OnConnectListener onConnectListener) {
        this.onConnectListener = onConnectListener;
    }


    public void connectMQTT() {
        //连接时使用的clientId, 必须唯一, 一般加时间戳
        String clientId = "dwayne" + System.currentTimeMillis();
        mMqttClient = new MqttAndroidClient(context, "tcp://110.42.159.133:1883", clientId);
        //连接参数
        MqttConnectOptions options;
        options = new MqttConnectOptions();
        //设置自动重连
        options.setAutomaticReconnect(true);
        // 缓存,
        options.setCleanSession(true);
        // 设置超时时间，单位：秒
        options.setConnectionTimeout(15);
        // 心跳包发送间隔，单位：秒
        options.setKeepAliveInterval(15);
        // 用户名
        options.setUserName("dwayne");
        // 密码
        options.setPassword("ebesin".toCharArray());
        // 设置MQTT监听
        mMqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "connectionLost: 连接断开");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "消息到达\t"+ "topic:"+topic+"\tmsg:"+message.toString());
                EventBus.getDefault().post(new MqttEvent(topic,message.toString()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        try {
            //进行连接
            mMqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: 连接成功");
                    isConnected = true;
                    try {
                        mMqttClient.subscribe("test", 0);
                        onConnectListener.OnConnectSuccess();
                    } catch (MqttException e) {
                        e.printStackTrace();
                        onConnectListener.OnConnectFail("订阅test话题失败\n错误代码："+e.getReasonCode());
                    }
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure: 连接MQTT服务失败"+exception.getMessage());
                    onConnectListener.OnConnectFail("连接MQTT服务失败,远程连接功能不可用\n错误代码："+asyncActionToken.getException().getReasonCode());
                }
            });
        } catch (MqttException e) {
            Log.d(TAG, "onFailure: 连接MQTT服务失败"+e.getMessage());
            onConnectListener.OnConnectFail("连接MQTT服务失败，远程连接功能不可用\n错误代码："+e.getReasonCode());
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public MqttAndroidClient getmMqttClient() {
        return mMqttClient;
    }

    public interface OnConnectListener{
        void OnConnectSuccess();
        void OnConnectFail(String failMsg);
    }
}
