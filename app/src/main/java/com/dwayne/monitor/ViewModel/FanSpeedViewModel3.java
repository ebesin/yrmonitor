package com.dwayne.monitor.ViewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.Objects;

public class FanSpeedViewModel3 extends ViewModel {
    private MutableLiveData<int []> speed = new MutableLiveData<>();
    private final int num;

    public FanSpeedViewModel3(int i) {
        num = i;
        int[] ints = new int[num];
        Arrays.fill(ints,0);
        speed.setValue(ints);
    }

    public MutableLiveData<int[]> getSpeed() {
        return speed;
    }

    public void setSpeed(MutableLiveData<int[]> speed) {
        this.speed = speed;
    }

    public void setSpeedValue(int [] args){
        speed.setValue(args);
    }

    public int[] getSpeedValue(){
        return speed.getValue();
    }

    public void setValueOnIndex(int index,int value){
        Objects.requireNonNull(speed.getValue())[index] = value;
    }

    public int getNum() {
        return num;
    }
}
