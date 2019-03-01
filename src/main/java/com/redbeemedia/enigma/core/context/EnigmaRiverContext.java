package com.redbeemedia.enigma.core.context;

import android.app.Application;

import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManagerFactory;
import com.redbeemedia.enigma.core.http.DefaultHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.device.DeviceInfo;
import com.redbeemedia.enigma.core.util.device.IDeviceInfo;

public final class EnigmaRiverContext {
    private static volatile EnigmaRiverInitializedContext initializedContext = null;

    public static synchronized void initialize(Application application) {
        EnigmaRiverContext.initialize(application, new EnigmaRiverContextInitialization());
    }

    public static synchronized void initialize(Application application, EnigmaRiverContextInitialization initialization) {
        if(initializedContext == null) {
            initializedContext = new EnigmaRiverInitializedContext(application, initialization);
        } else {
            throw new IllegalStateException("EnigmaRiverContext already initialized.");
        }
    }

    /**
     * For unit tests.
     * @param initialization
     */
    /*package-protected*/ static synchronized void resetInitialization(EnigmaRiverContextInitialization initialization) {
        initializedContext = new EnigmaRiverInitializedContext(null, initialization);
    }

    public static UrlPath getExposureBaseUrl() {
        //TODO assert initialized
        return initializedContext.exposureBaseUrl;
    }

    public static IHttpHandler getHttpHandler() {
        //TODO assert initialized
        return initializedContext.httpHandler;
    }

    public static IDeviceInfo getDeviceInfo() {
        //TODO assert initialized
        return initializedContext.deviceInfo;
    }

    public static IActivityLifecycleManager getActivityLifecycleManager() {
        //TODO assert initialized
        return initializedContext.activityLifecycleManager;
    }


    public static class EnigmaRiverContextInitialization {
        private IHttpHandler httpHandler = null;
        private String exposureBaseUrl = null;
        private IDeviceInfo deviceInfo = null;
        private IActivityLifecycleManagerFactory activityLifecycleManagerFactory = new DefaultActivityLifecycleManagerFactory();

        public String getExposureBaseUrl() {
            return exposureBaseUrl;
        }

        public EnigmaRiverContextInitialization setExposureBaseUrl(String exposureBaseUrl) {
            this.exposureBaseUrl = exposureBaseUrl;
            return this;
        }

        public IHttpHandler getHttpHandler()
        {
            if(httpHandler == null) {
                httpHandler = new DefaultHttpHandler();
            }

            return httpHandler;
        }

        public EnigmaRiverContextInitialization setHttpHandler(final IHttpHandler httpHandler) {
            this.httpHandler = httpHandler;
            return this;
        }

        public IDeviceInfo getDeviceInfo(final Application application) {
            if(deviceInfo != null) {
                return deviceInfo;
            } else {
                return new DeviceInfo(application);
            }
        }
        
        public EnigmaRiverContextInitialization setDeviceInfo(final IDeviceInfo deviceInfo) {
            this.deviceInfo = deviceInfo;
            return this;
        }

        public EnigmaRiverContextInitialization setActivityLifecycleManagerFactory(IActivityLifecycleManagerFactory activityLifecycleManagerFactory) {
            this.activityLifecycleManagerFactory = activityLifecycleManagerFactory;
            return this;
        }

        public IActivityLifecycleManager getActivityLifecycleManager(Application application) {
            return activityLifecycleManagerFactory.createActivityLifecycleManager(application);
        }
    }

    private static class EnigmaRiverInitializedContext {
        private final UrlPath exposureBaseUrl;
        private final IHttpHandler httpHandler;
        private final IDeviceInfo deviceInfo;
        private final IActivityLifecycleManager activityLifecycleManager;

        public EnigmaRiverInitializedContext(Application application, EnigmaRiverContextInitialization initialization) {
            try {
                String baseUrl = initialization.getExposureBaseUrl();
                if(baseUrl == null) {
                    throw new IllegalStateException("No exposure base url supplied.");
                }
                this.exposureBaseUrl = new UrlPath(baseUrl);
                this.httpHandler = initialization.getHttpHandler();
                this.deviceInfo = initialization.getDeviceInfo(application);
                this.activityLifecycleManager = initialization.getActivityLifecycleManager(application);
            } catch (Exception e) {
                //TODO throw ContextInitializationException
                throw new RuntimeException(e);
            }
        }
    }
}
