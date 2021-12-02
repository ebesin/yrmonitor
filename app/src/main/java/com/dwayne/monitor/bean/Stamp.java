package com.dwayne.monitor.bean;

public class Stamp {
    private int nsecs;

    private int secs;

    public Stamp(int nsecs, int secs) {
        this.nsecs = nsecs;
        this.secs = secs;
    }

    public void setNsecs(int nsecs){
        this.nsecs = nsecs;
    }
    public int getNsecs(){
        return this.nsecs;
    }
    public void setSecs(int secs){
        this.secs = secs;
    }
    public int getSecs(){
        return this.secs;
    }
}
