package com.dwayne.monitor.bean;

public class Angular {
    private double x;

    private double y;

    private double z;

    public Angular(double z) {
        this.z = z;
        x = 0;
        y = 0;
    }

    public void setX(double x){
        this.x = x;
    }
    public double getX(){
        return this.x;
    }
    public void setY(double y){
        this.y = y;
    }
    public double getY(){
        return this.y;
    }
    public void setZ(double z){
        this.z = z;
    }
    public double getZ(){
        return this.z;
    }
}
