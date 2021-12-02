package com.example.dadac.testrosbridge;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.runner.RunWith;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.channels.MulticastChannel;

@RunWith(AndroidJUnit4.class)
public class TestMqtt {
    private static final String TAG = "MqttClient";
    @Test
    public void testMqtt(){
        Context context = ApplicationProvider.getApplicationContext();
        String clientId = "dwayne" + System.currentTimeMillis();
        MqttAndroidClient mqttAndroidClient = new MqttAndroidClient(context,"110.42.159.133",clientId);
        MqttConnectOptions options;
        options = new MqttConnectOptions();
        options.setUserName("dwayne");
        // 密码
        options.setPassword("ebesin".toCharArray());
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "connectionLost: 连接断开");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "消息到达");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        try {
            mqttAndroidClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: 连接成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure: 连接失败");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
