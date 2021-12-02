package com.dwayne.monitor.dao;

public interface PortImpl {
    String getPortByDeviceId(String id);
    void updatePortByDeviceId(String id);
}
