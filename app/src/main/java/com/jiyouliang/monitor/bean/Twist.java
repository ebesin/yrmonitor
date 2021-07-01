package com.jiyouliang.monitor.bean;

public class Twist {
    private Linear linear;
    private Angular angular;

    public Twist(Linear linear, Angular angular) {
        this.linear = linear;
        this.angular = angular;
    }

    public void setAngular(Angular angular){
        this.angular = angular;
    }
    public Angular getAngular(){
        return this.angular;
    }
    public void setLinear(Linear linear){
        this.linear = linear;
    }
    public Linear getLinear(){
        return this.linear;
    }
}
