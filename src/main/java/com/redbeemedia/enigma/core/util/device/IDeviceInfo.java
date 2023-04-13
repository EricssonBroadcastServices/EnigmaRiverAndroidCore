package com.redbeemedia.enigma.core.util.device;

public interface IDeviceInfo {
    String getOS();
    String getOSVersion();
    String getManufacturer();
    boolean isDeviceRooted();
    String getWidevineDrmSecurityLevel();
    int getWidthPixels();
    int getHeightPixels();
    String getName();
    String getDeviceId();
    String getDeviceModelLogin();
    // example :  android-<tv/tablet/mobile>-SM-G970F
    String getDeviceModelPlay();
    String getDeviceTypeLogin();
    // ctv / tablet / mobile
    String getDeviceTypePlay();
    String getAppType();
    String getGoogleAdId();
    boolean isLimitedAdTracking();
}
