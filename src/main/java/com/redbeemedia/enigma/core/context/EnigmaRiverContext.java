package com.redbeemedia.enigma.core.context;

import android.app.Application;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManagerFactory;
import com.redbeemedia.enigma.core.epg.IEpgLocator;
import com.redbeemedia.enigma.core.http.DefaultHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.device.DeviceInfo;
import com.redbeemedia.enigma.core.util.device.IDeviceInfo;

public final class EnigmaRiverContext {
    private static volatile EnigmaRiverInitializedContext initializedContext = null;

    public static synchronized void initialize(Application application, String exposureBaseUrl) {
        EnigmaRiverContext.initialize(application, new EnigmaRiverContextInitialization(exposureBaseUrl));
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
        assertInitialized();
        return initializedContext.exposureBaseUrl;
    }

    public static IHttpHandler getHttpHandler() {
        assertInitialized();
        return initializedContext.httpHandler;
    }

    public static IDeviceInfo getDeviceInfo() {
        assertInitialized();
        return initializedContext.deviceInfo;
    }

    public static IActivityLifecycleManager getActivityLifecycleManager() {
        assertInitialized();
        return initializedContext.activityLifecycleManager;
    }

    public static ITaskFactory getTaskFactory() {
        assertInitialized();
        return initializedContext.taskFactory;
    }

    public static IEpgLocator getEpgLocator() {
        assertInitialized();
        return initializedContext.epgLocator;
    }

    //Version if the core library
    public static String getVersion() {
        String version = "r1.0.31";
        if(version.contains("REPLACE_WITH_RELEASE_VERSION")) {
            return "dev-snapshot-"+BuildConfig.VERSION_NAME;
        } else {
            return version;
        }
    }

    private static void assertInitialized() {
        if(initializedContext == null) {
            throw new IllegalStateException("EnigmaRiverContext is not initialized.");
        }
    }

    public static class EnigmaRiverContextInitialization {
        private IHttpHandler httpHandler = null;
        private String exposureBaseUrl = null;
        private IDeviceInfo deviceInfo = null;
        private IActivityLifecycleManagerFactory activityLifecycleManagerFactory = new DefaultActivityLifecycleManagerFactory();
        private ITaskFactory taskFactory = new DefaultTaskFactory();
        private IEpgLocator epgLocator = new DefaultEpgLocator();

        public EnigmaRiverContextInitialization(String exposureBaseUrl) {
            this.exposureBaseUrl = exposureBaseUrl;
        }

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

        public ITaskFactory getTaskFactory() {
            return taskFactory;
        }

        protected EnigmaRiverContextInitialization setTaskFactory(ITaskFactory taskFactory) {
            this.taskFactory = taskFactory;
            return this;
        }

        public IEpgLocator getEpgLocator() {
            return epgLocator;
        }

        public EnigmaRiverContextInitialization setEpgLocator(IEpgLocator epgLocator) {
            this.epgLocator = epgLocator;
            return this;
        }
    }

    private static class EnigmaRiverInitializedContext {
        private final UrlPath exposureBaseUrl;
        private final IHttpHandler httpHandler;
        private final IDeviceInfo deviceInfo;
        private final IActivityLifecycleManager activityLifecycleManager;
        private final ITaskFactory taskFactory;
        private final IEpgLocator epgLocator;

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
                this.taskFactory = initialization.getTaskFactory();
                this.epgLocator = initialization.getEpgLocator();
            } catch (Exception e) {
                //TODO throw ContextInitializationException
                throw new RuntimeException(e);
            }
        }
    }
}
