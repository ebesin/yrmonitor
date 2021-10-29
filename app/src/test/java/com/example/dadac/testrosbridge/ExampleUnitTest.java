package com.example.dadac.testrosbridge;

import com.dwayne.monitor.HunterActivity;
import com.dwayne.monitor.NewBunkerActivity;
import com.dwayne.monitor.enums.ConnectMode;
import com.dwayne.monitor.view.model.HunterModelView;
import com.google.gson.Gson;
import com.jilk.ros.ROSClient;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.dwayne.monitor.OldBunkerActivity;
import com.dwayne.monitor.RosBridgeActivity;
import com.dwayne.monitor.bean.Angular;
import com.dwayne.monitor.bean.Header;
import com.dwayne.monitor.bean.Linear;
import com.dwayne.monitor.bean.Spray;
import com.dwayne.monitor.bean.Stamp;
import com.dwayne.monitor.bean.Status;
import com.dwayne.monitor.bean.Twist;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.junit.Test;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

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
    public void testConn() {
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

    @Test
    public void testString() {
        String msg2 = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0.5 + "}}}";
        System.out.println(msg2);
    }

    @Test
    public void testString2(){
        Status status = new Status(1.11111,2.222222,3.333333,4.4444,5.55555);
        String data = new Gson().toJson(status);
        String msg1 = "{ \"op\": \"publish\", \"topic\": \"/chatter\", \"msg\": " + data + "}";
        System.out.println(msg1);
    }

    @Test
    public void testTwist(){
        Linear linear = new Linear(0.2);
        Angular angular = new Angular(0);
        Twist twist = new Twist(linear,angular);
        System.out.println(new Gson().toJson(twist));
    }

    @Test
    public void testSpray(){
        Stamp stamp = new Stamp(0,0);
        Header header = new Header(stamp,"test",87);
        int[] duc_array = new int[9];
        Spray spray = new Spray(header,duc_array);
        System.out.println(new Gson().toJson(spray));
    }

    @Test
    public void testList(){
        List<Integer> ints = new ArrayList<>();
        ints.add(1);
        ints.add(1);
        ints.add(1);
        ints.add(1);
        ints.add(1);
        System.out.println(new Gson().toJson(ints));
    }

    @Test
    public void testClass() throws ClassNotFoundException {
        System.out.println(OldBunkerActivity.class);
        System.out.println(NewBunkerActivity.class);
        System.out.println(HunterActivity.class);
        System.out.println(RosBridgeActivity.class);
    }

    @Test
    public void testMqtt(){

    }

    @Test
    public void testEnum(){
        System.out.println(ConnectMode.REMOTEMODE.getMode());
    }
}