package com.jiyouliang.fmap.ViewModel;

import android.arch.lifecycle.MutableLiveData;

import com.jiyouliang.fmap.bean.Battery;

public class BatteryViewModel {
    MutableLiveData<Battery> batteryMutableLiveData;

    public BatteryViewModel() {
        batteryMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Battery> getBatteryMutableLiveData() {
        return batteryMutableLiveData;
    }

    public void setBatteryMutableLiveData(MutableLiveData<Battery> batteryMutableLiveData) {
        this.batteryMutableLiveData = batteryMutableLiveData;
    }

    public void setValue(Battery battery){
        batteryMutableLiveData.setValue(battery);
    }
}
