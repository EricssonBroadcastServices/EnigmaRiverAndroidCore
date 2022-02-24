package com.redbeemedia.enigma.core.util.device;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class DeviceInfoTest {
    @Test
    public void testGetDeviceInfoJson() throws JSONException {
        FakeDeviceInfo fakeDeviceInfo = new FakeDeviceInfo();
        JSONObject jsonObject = DeviceInfo.getDeviceInfoJson(fakeDeviceInfo);
        //Only 'type' is currently required from the backend API
        Assert.assertEquals(fakeDeviceInfo.getType(), jsonObject.getString("type"));
    }

    private static class FakeDeviceInfo implements IDeviceInfo {
        @Override
        public String getDeviceId() {
            return "fake_id";
        }

        @Override
        public String getModel() {
            return "fake_model";
        }

        @Override
        public String getOS() {
            return "fake_OS";
        }

        @Override
        public String getOSVersion() {
            return "fake_OSVersion";
        }

        @Override
        public String getManufacturer() {
            return "fake_Manufacturer";
        }

        @Override
        public boolean isDeviceRooted() {
            return false;
        }

        @Override
        public String getWidevineDrmSecurityLevel() {
            return "N/A (Fake)";
        }

        @Override
        public int getWidthPixels() {
            return 1920;
        }

        @Override
        public int getHeightPixels() {
            return 1080;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getType() {
            return "JUNIT_TEST_DEVICE";
        }

        @Override
        public String getAppType() {
            return "mock-app-type";
        }
    }
}
