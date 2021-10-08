package com.dwayne.monitor.ViewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.dwayne.monitor.bean.Status;

public class StatusViewModel extends ViewModel {
    MutableLiveData<Status> statusMutableLiveData;

    public StatusViewModel() {
        this.statusMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Status> getStatusMutableLiveData() {
        return statusMutableLiveData;
    }

    public void setStatusMutableLiveData(MutableLiveData<Status> statusMutableLiveData) {
        this.statusMutableLiveData = statusMutableLiveData;
    }

    public void setValue(Status status){
        statusMutableLiveData.setValue(status);
    }
}
