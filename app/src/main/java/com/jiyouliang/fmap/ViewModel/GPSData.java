package com.jiyouliang.fmap.ViewModel;

public class GPSData {
    double lng;
    double lat;

    public GPSData(double lat, double lng) {
        this.lng = lng;
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
