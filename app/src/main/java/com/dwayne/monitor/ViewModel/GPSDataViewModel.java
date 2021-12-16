package com.dwayne.monitor.ViewModel;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GPSDataViewModel extends ViewModel {

    private MutableLiveData<GPSData> gpsLiveData;

    public GPSDataViewModel() {
        GPSData gpsData = new GPSData(35.694,139.749);
        gpsLiveData = new MutableLiveData<>();
        gpsLiveData.setValue(gpsData);
    }

    public MutableLiveData<GPSData> getGpsData() {
        return gpsLiveData;
    }

    public void setGpsData(MutableLiveData<GPSData> gpsLiveData) {
        this.gpsLiveData = gpsLiveData;
    }

    public GPSData getValue(){
        return gpsLiveData.getValue();
    }

    public void setData(GPSData gpsData){
        gpsLiveData.setValue(gpsData);
    }

}
