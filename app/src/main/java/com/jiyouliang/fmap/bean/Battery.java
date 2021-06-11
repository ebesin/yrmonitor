package com.jiyouliang.fmap.bean;

public class Battery {
    int power;
    float Voltage;

    public Battery() {
    }

    public Battery(int power, float voltage) {
        this.power = power;
        Voltage = voltage;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public float getVoltage() {
        return Voltage;
    }

    public void setVoltage(float voltage) {
        Voltage = voltage;
    }
}
