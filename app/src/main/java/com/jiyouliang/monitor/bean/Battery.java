package com.jiyouliang.monitor.bean;

public class Battery {
    int power;
    float voltage;

    public Battery() {
    }

    public Battery(int power, float voltage) {
        this.power = power;
        this.voltage = voltage;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }
}
