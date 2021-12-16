package com.dwayne.monitor.enums;

import java.io.Serializable;

@SuppressWarnings("all")
public enum ConnectMode implements Serializable {
    REMOTEMODE("远程连接"),LANMODE("局域网连接"),TESTMODE("界面测试(不连接)");
    private String mode;

    private ConnectMode(String mode) {
        this.mode = mode;
    }

    public String getMode(){
        return mode;
    }
}
