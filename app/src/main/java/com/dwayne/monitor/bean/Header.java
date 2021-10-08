package com.dwayne.monitor.bean;

public class Header {
    private Stamp stamp;

    private String frame_id;

    private int seq;

    public Header(Stamp stamp, String frame_id, int seq) {
        this.stamp = stamp;
        this.frame_id = frame_id;
        this.seq = seq;
    }

    public void setStamp(Stamp stamp){
        this.stamp = stamp;
    }
    public Stamp getStamp(){
        return this.stamp;
    }
    public void setFrame_id(String frame_id){
        this.frame_id = frame_id;
    }
    public String getFrame_id(){
        return this.frame_id;
    }
    public void setSeq(int seq){
        this.seq = seq;
    }
    public int getSeq(){
        return this.seq;
    }
}
