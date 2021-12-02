package com.dwayne.monitor.dao;

import com.dwayne.monitor.bean.Device;

import java.util.BitSet;
import java.util.List;

public interface DeviceImpl {
    List<Device> getAllDevices();

    Device getDeviceById();

    boolean updateNameById(String id, String name);

    boolean updateNameAndIpById(String id, String name, String ip);

    boolean updataAllInfoById(String id, String name, String ip,String type,String connectMode,String port);

}
