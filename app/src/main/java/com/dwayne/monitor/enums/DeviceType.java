package com.dwayne.monitor.enums;

public enum DeviceType {
    OLDBUNKER("履带车v1"),HUNTER("阿克曼车"),NEWBUNKER("履带车v2");
    private String type;

    private DeviceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
