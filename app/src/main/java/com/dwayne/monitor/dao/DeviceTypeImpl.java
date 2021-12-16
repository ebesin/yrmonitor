package com.dwayne.monitor.dao;

import java.util.List;

public interface DeviceTypeImpl {
    List<String> getAllTypeName();
    String getIntentClassByName(String name);
}
