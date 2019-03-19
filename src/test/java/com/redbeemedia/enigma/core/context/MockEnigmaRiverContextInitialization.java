package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.activity.IActivityLifecycleManagerFactory;
import com.redbeemedia.enigma.core.activity.MockActivityLifecycleManagerFactory;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.MockTaskFactory;
import com.redbeemedia.enigma.core.util.device.IDeviceInfo;
import com.redbeemedia.enigma.core.util.device.MockDeviceInfo;

public class MockEnigmaRiverContextInitialization extends EnigmaRiverContext.EnigmaRiverContextInitialization {
    public MockEnigmaRiverContextInitialization() {
        super("https://mock.unittests.example.com");
        setDeviceInfo(new MockDeviceInfo());
        setHttpHandler(new MockHttpHandler());
        setActivityLifecycleManagerFactory(new MockActivityLifecycleManagerFactory());
        setTaskFactory(new MockTaskFactory());
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

    @Override
    public MockEnigmaRiverContextInitialization setActivityLifecycleManagerFactory(IActivityLifecycleManagerFactory activityLifecycleManagerFactory) {
        return (MockEnigmaRiverContextInitialization) super.setActivityLifecycleManagerFactory(activityLifecycleManagerFactory);
    }

    @Override
    public MockEnigmaRiverContextInitialization setTaskFactory(ITaskFactory taskFactory) {
        return (MockEnigmaRiverContextInitialization) super.setTaskFactory(taskFactory);
    }
}
