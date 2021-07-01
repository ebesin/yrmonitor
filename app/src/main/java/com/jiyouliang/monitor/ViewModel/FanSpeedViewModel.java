package com.jiyouliang.monitor.ViewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Arrays;

public class FanSpeedViewModel extends ViewModel {
    private MutableLiveData<int []> speed = new MutableLiveData<>();

    public FanSpeedViewModel() {
        int[] ints = new int[9];
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
