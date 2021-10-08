package com.dadac.testrosbridge;

import android.app.Application;

import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.dwayne.monitor.database.DataBaseUtil;
import com.dwayne.monitor.database.DatabaseHelper;

/**
 * @ Create by dadac on 2018/10/8.
 * @Function:
 * @Return:
 */
public class RCApplication extends Application {

    String ip;
    ROSBridgeClient client;
    boolean isConn = false;
    DatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this);
        DataBaseUtil.setDatabaseHelper(databaseHelper);
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

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }


}



