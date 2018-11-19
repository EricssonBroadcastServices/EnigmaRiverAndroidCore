package com.redbeemedia.enigma.core.util.device;

public class MockDeviceInfo implements IDeviceInfo {
    @Override
    public String getDeviceId() {
        return "JUnitDevice";
    }

    @Override
    public String getModel() {
        return "model";
    }

    @Override
    public String getOS() {
        return "os";
    }

    @Override
    public String getOSVersion() {
        return "v0";
    }

    @Override
    public String getManufacturer() {
        return "manufacturer";
    }

    @Override
    public boolean isDeviceRooted() {
        return false;
    }

    @Override
    public String getWidevineDrmSecurityLevel() {
        return "N/A (JUnit)";
    }

    @Override
    public int getWidthPixels() {
        return 640;
    }

    @Override
    public int getHeightPixels() {
        return 480;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getType() {
        return "MOCK";
    }
}
