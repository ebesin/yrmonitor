package com.jiyouliang.monitor.bean;

//topic:motion_com

public class MotionCom {
    int Forward;
    int Back;
    int TurnLeft;
    int TurnRight;
    int Stop;

    public MotionCom() {
        Forward = 0;
        Back = 0;
        TurnLeft = 0;
        TurnRight = 0;
        Stop = 0;
    }

    public MotionCom(int forward, int back, int turnLeft, int turnRight, int stop) {
        Forward = forward;
        Back = back;
        TurnLeft = turnLeft;
        TurnRight = turnRight;
        Stop = stop;
    }

    public void setValues(int forward, int back, int turnLeft, int turnRight, int stop){
        Forward = forward;
        Back = back;
        TurnLeft = turnLeft;
        TurnRight = turnRight;
        Stop = stop;
    }


    public int getForward() {
        return Forward;
    }

    public void setForward(int forward) {
        Forward = forward;
    }

    public int getBack() {
        return Back;
    }

    public void setBack(int back) {
        Back = back;
    }

    public int getTurnLeft() {
        return TurnLeft;
    }

    public void setTurnLeft(int turnLeft) {
        TurnLeft = turnLeft;
    }

    public int getTurnRight() {
        return TurnRight;
    }

    public void setTurnRight(int turnRight) {
        TurnRight = turnRight;
    }

    public int getStop() {
        return Stop;
    }

    public void setStop(int stop) {
        Stop = stop;
    }
}
