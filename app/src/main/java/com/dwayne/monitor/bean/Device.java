package com.dwayne.monitor.bean;

import android.widget.TextView;

public class Device {
    String id;
    String name;
    String ip;
    String intentClass;
    TextView name_textView;
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIntentClass() {
        return intentClass;
    }

    public void setIntentClass(String intentClass) {
        this.intentClass = intentClass;
    }


    public TextView getName_textView() {
        return name_textView;
    }

    public void setName_textView(TextView name_textView) {
        this.name_textView = name_textView;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Device(String id, String name, String ip) {
        this.id = id;
        this.name = name;
        this.ip = ip;
    }

    public Device(String id, String name, String ip,String type) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


}
