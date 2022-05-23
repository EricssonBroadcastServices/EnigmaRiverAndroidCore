package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.testutil.MockApplication;
import com.redbeemedia.enigma.core.util.device.MockDeviceInfo;

import org.junit.Assert;
import org.junit.Test;

public class EnigmaRiverContextTest {
    @Test
    public void testUpdateInit() {
        MockEnigmaRiverContextInitialization initialization = new MockEnigmaRiverContextInitialization();
       // EnigmaRiverContext.initialize(new MockApplication(), initialization);

        MockDeviceInfo mockDeviceInfo = new MockDeviceInfo();

        Assert.assertEquals(EnigmaRiverContext.getAppName(), "");
        Assert.assertEquals(EnigmaRiverContext.getExposureBaseUrl().toString(), "https://mock.unittests.example.com");
        Assert.assertNull(EnigmaRiverContext.getAnalyticsUrl());
        Assert.assertEquals(EnigmaRiverContext.getDeviceInfo().getDeviceId(), mockDeviceInfo.getDeviceId());

        MockEnigmaRiverContextInitialization newInitialization = new MockEnigmaRiverContextInitialization();
        newInitialization.setAnalyticsUrl("http://www.analyticsurl.com");
        newInitialization.setExposureBaseUrl("http://www.exposureurl.com");
        newInitialization.setAppName("TestNewApp");
        mockDeviceInfo.setDeviceId("NewId");
        newInitialization.setDeviceInfo(mockDeviceInfo);
        MockApplication application = new MockApplication();

        EnigmaRiverContext.updateInitialization(newInitialization);

        //Verify if it is updated
        Assert.assertEquals(EnigmaRiverContext.getAppName(), "TestNewApp");
        Assert.assertEquals(EnigmaRiverContext.getExposureBaseUrl().toString(), "http://www.exposureurl.com");
        Assert.assertEquals(EnigmaRiverContext.getAnalyticsUrl().toString(), "http://www.analyticsurl.com");
        Assert.assertEquals(EnigmaRiverContext.getDeviceInfo().getDeviceId(), "NewId");
    }
}