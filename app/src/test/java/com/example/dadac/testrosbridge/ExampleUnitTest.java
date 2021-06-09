package com.example.dadac.testrosbridge;

import android.util.Log;

import com.dadac.testrosbridge.RCApplication;
import com.jilk.ros.ROSClient;
import com.jilk.ros.rosbridge.ROSBridgeClient;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    ROSBridgeClient client;
    String ip = "192.168.1.100";   //虚拟机的 IP
    // String ip = "192.168.10.20";     //半残废机器人的IP
    // String ip = "192.168.10.200";     //机器人的IP
    String port = "9090";

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testConn(){
        client = new ROSBridgeClient("ws://" + ip + ":" + port);
        boolean conneSucc = client.connect(new ROSClient.ConnectionStatusListener() {
            @Override
            public void onConnect() {
                client.setDebug(true);
                String data = "hello";
                String msg1 = "{ \"op\": \"publish\", \"topic\": \"/dwayne\", \"msg\": { \"data\": \"" + data + " \" }}";
//                Log.d("dachen", "Connect ROS success");
                client.send(msg1);
                System.out.println("Connect ROS success");
            }

            @Override
            public void onDisconnect(boolean normal, String reason, int code) {
//                Log.d("dachen", "ROS disconnect");
                System.out.println("ROS disconnect");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
//                Log.d("dachen", "ROS communication error");
                System.out.println("ROS communication error");
            }
        });
        client.disconnect();
    }
}

