package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.util.device.IDeviceInfo;
import com.redbeemedia.enigma.core.util.device.MockDeviceInfo;

public class MockEnigmaRiverContextInitialization extends EnigmaRiverContext.EnigmaRiverContextInitialization {
    public MockEnigmaRiverContextInitialization() {
        setDeviceInfo(new MockDeviceInfo());
        setHttpHandler(new MockHttpHandler());
    }

    @Override
    public MockEnigmaRiverContextInitialization setDeviceInfo(IDeviceInfo deviceInfo) {
        return (MockEnigmaRiverContextInitialization) super.setDeviceInfo(deviceInfo);
    }

    @Override
    public MockEnigmaRiverContextInitialization setHttpHandler(IHttpHandler httpHandler) {
        return (MockEnigmaRiverContextInitialization) super.setHttpHandler(httpHandler);
    }

    @Override
    public MockEnigmaRiverContextInitialization setExposureBaseUrl(String exposureBaseUrl) {
        return (MockEnigmaRiverContextInitialization) super.setExposureBaseUrl(exposureBaseUrl);
    }
}
