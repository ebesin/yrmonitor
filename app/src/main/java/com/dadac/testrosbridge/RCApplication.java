package com.dadac.testrosbridge;

import android.app.Application;

import com.jilk.ros.rosbridge.ROSBridgeClient;

/**
 * @ Create by dadac on 2018/10/8.
 * @Function:
 * @Return:
 */
public class RCApplication extends Application {

    String ip;
    ROSBridgeClient client;
    boolean isConn = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        if (client != null)
            client.disconnect();
        super.onTerminate();

    }

    public void setIp(String ip){
        this.ip = ip;
    }


    public String getIp(){
        return ip;
    }

    public boolean isConn() {
        return isConn;
    }

    public void setConn(boolean conn) {
        isConn = conn;
    }

    public ROSBridgeClient getRosClient() {
        return client;
    }

    public void setRosClient(ROSBridgeClient client) {
        this.client = client;
    }
}



