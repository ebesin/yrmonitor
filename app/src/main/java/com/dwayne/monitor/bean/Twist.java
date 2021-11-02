package com.dwayne.monitor.bean;

public class Twist {
    private Linear linear;
    private Angular angular;

    public Twist(Linear linear, Angular angular) {
        this.linear = linear;
        this.angular = angular;
    }

    public Twist() {
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

    public void setData(Linear linear, Angular angular){
        this.linear = linear;
        this.angular = angular;
    }
}
