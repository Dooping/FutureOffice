package com.office.future.futureoffice;

import java.io.Serializable;

/**
 * Created by Dooping on 04/04/2016.
 */
public class MyConfig implements Serializable {
    private static final long serialVersionUID = 0L;
    public static final String DEFAULT_SERVER_ADDRESS = "192.168.1.4:8080";

    private double latitude;
    private double longitude;
    private String address;
    private String serverAddress;

    public MyConfig(){
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
}
