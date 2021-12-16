package com.dwayne.monitor.bean;

public class Status {

    private double lng;
    private double yaw_angle;
    private double front_wheel_angle;
    private double lat;
    private double speed;

    public Status(double lng, double yaw_angle, double front_wheel_angle, double lat, double speed) {
        this.lng = lng;
        this.yaw_angle = yaw_angle;
        this.front_wheel_angle = front_wheel_angle;
        this.lat = lat;
        this.speed = speed;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
    public double getLng() {
        return lng;
    }

    public void setYaw_angle(double yaw_angle) {
        this.yaw_angle = yaw_angle;
    }
    public double getYaw_angle() {
        return yaw_angle;
    }

    public void setFront_wheel_angle(double front_wheel_angle) {
        this.front_wheel_angle = front_wheel_angle;
    }
    public double getFront_wheel_angle() {
        return front_wheel_angle;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLat() {
        return lat;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
    public double getSpeed() {
        return speed;
    }

}
