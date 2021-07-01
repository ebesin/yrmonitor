package com.jiyouliang.monitor.ViewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.jiyouliang.monitor.bean.Battery;

public class BatteryViewModel extends ViewModel {
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
