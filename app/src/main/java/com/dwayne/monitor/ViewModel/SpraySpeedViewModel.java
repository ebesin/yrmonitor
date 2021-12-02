package com.dwayne.monitor.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.Objects;

public class SpraySpeedViewModel extends ViewModel {
    private MutableLiveData<int []> speed = new MutableLiveData<>();
    private final int num;

    public SpraySpeedViewModel(int i) {
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
