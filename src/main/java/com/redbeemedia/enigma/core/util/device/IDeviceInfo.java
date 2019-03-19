package com.redbeemedia.enigma.core.util.device;

public interface IDeviceInfo {
    String getDeviceId();
    String getModel();
    String getOS();
    String getOSVersion();
    String getManufacturer();
    boolean isDeviceRooted();
    String getWidevineDrmSecurityLevel();
    int getWidthPixels();
    int getHeightPixels();
    String getName();
    String getType();
}
