package com.jiyouliang.monitor.bean;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.widget.TextView;

public class Device {
    String id;
    String name;
    String ip;
    Class aClass;
    TextView name_textView;

    public Class getaClass() {
        return aClass;
    }

    public void setaClass(Class aClass) {
        this.aClass = aClass;
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
