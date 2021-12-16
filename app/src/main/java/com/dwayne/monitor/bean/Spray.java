package com.dwayne.monitor.bean;

public class Spray {
    private Header header;

    private int[] duc_array ;

    public Spray(Header header, int[] duc_array) {
        this.header = header;
        this.duc_array = duc_array;
    }

    public void setHeader(Header header){
        this.header = header;
    }
    public Header getHeader(){
        return this.header;
    }
    public void setDuc_array(int[] duc_array){
        this.duc_array = duc_array;
    }
    public int[] getDuc_array(){
        return this.duc_array;
    }
}
