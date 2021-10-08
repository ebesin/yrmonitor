package com.dwayne.monitor.ViewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Arrays;

public class FanSpeedViewModel2 extends ViewModel {
    private MutableLiveData<int []> speed = new MutableLiveData<>();

    public FanSpeedViewModel2() {
        int[] ints = new int[8];
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
        speed.getValue()[index] = value;
    }
}
