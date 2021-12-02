package com.dwayne.monitor.bean;

public class Linear {
    private double x;

    private double y;

    private double z;

    public Linear(double x) {
        this.x = x;
        y = 0;
        z = 0;
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
