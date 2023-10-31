package com.redbeemedia.enigma.core.context;

import android.app.Application;

import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManagerFactory;
import com.redbeemedia.enigma.core.ads.DeviceParameters;
import com.redbeemedia.enigma.core.ads.IAdInsertionFactory;
import com.redbeemedia.enigma.core.analytics.EnigmaStorageManager;
import com.redbeemedia.enigma.core.context.exception.ContextInitializationException;
import com.redbeemedia.enigma.core.epg.IEpgLocator;
import com.redbeemedia.enigma.core.http.DefaultHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.network.IDefaultNetworkMonitor;
import com.redbeemedia.enigma.core.network.INetworkMonitor;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.device.DeviceInfo;
import com.redbeemedia.enigma.core.util.device.IDeviceInfo;

import java.util.HashMap;
import java.util.Map;

public final class EnigmaRiverContext {
    private static volatile EnigmaRiverInitializedContext initializedContext = null;

    public static synchronized void initialize(Application application, String exposureBaseUrl) throws ContextInitializationException {
        EnigmaRiverContext.initialize(application, new EnigmaRiverContextInitialization(exposureBaseUrl));
    }

    public static synchronized void initialize(Application application, EnigmaRiverContextInitialization initialization) throws ContextInitializationException {
        try {
            if (application == null) {
                throw new NullPointerException("application was null");
            }
            if (initializedContext == null) {
                initializedContext = new EnigmaRiverInitializedContext(application, initialization);
                EnigmaModuleInitializer.initializeModules(new ModuleContextInitialization(application, initialization.moduleSettings));
            } else {
                throw new IllegalStateException("EnigmaRiverContext already initialized.");
            }
        } catch (ContextInitializationException e) {
            throw e;
        } catch (Exception e) {
            throw new ContextInitializationException(e);
        }
    }

    public static synchronized void updateInitialization(EnigmaRiverContextInitialization requestedContext) {
        if (requestedContext.exposureBaseUrl != null) {
            initializedContext.exposureBaseUrl = new UrlPath(requestedContext.exposureBaseUrl);
        }
        if (requestedContext.deviceInfo != null) {
            initializedContext.deviceInfo = requestedContext.deviceInfo;
        }
        if (requestedContext.analyticsUrl != null) {
            initializedContext.analyticsUrl = new UrlPath(requestedContext.analyticsUrl);
        }
        if (requestedContext.adInsertionFactory != null) {
            initializedContext.adInsertionFactory = requestedContext.adInsertionFactory;
        }
        if (requestedContext.appName != null) {
            initializedContext.appName = requestedContext.appName;
        }
    }

    /**
     * For unit tests.
     *
     * @param initialization
     */
    /*package-protected*/
    static synchronized void resetInitialization(EnigmaRiverContextInitialization initialization) {
        initializedContext = new EnigmaRiverInitializedContext(null, initialization);
    }

    public static UrlPath getAnalyticsUrl() {
        assertInitialized();
        return initializedContext.analyticsUrl;
    }

    public static UrlPath getExposureBaseUrl() {
        assertInitialized();
        return initializedContext.exposureBaseUrl;
    }

    public static EnigmaStorageManager getEnigmaStorageManager() {
        assertInitialized();
        return initializedContext.storageManager;
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

    public static IAdInsertionFactory getAdInsertionFactory() {
        assertInitialized();
        return initializedContext.adInsertionFactory;
    }

    public static DeviceParameters getDeviceParameters() {
        assertInitialized();
        return initializedContext.getDeviceParameters();
    }

    /**
     * @deprecated Use {@link #getTaskFactoryProvider()} <br> Ex: {@code getTaskFactoryProvider().getTaskFactory()}
     */
    @Deprecated
    public static ITaskFactory getTaskFactory() {
        assertInitialized();
        return initializedContext.taskFactoryProvider.getTaskFactory();
    }

    public static ITaskFactoryProvider getTaskFactoryProvider() {
        assertInitialized();
        return initializedContext.taskFactoryProvider;
    }

    public static IEpgLocator getEpgLocator() {
        assertInitialized();
        return initializedContext.epgLocator;
    }

    public static INetworkMonitor getNetworkMonitor() {
        assertInitialized();
        return initializedContext.networkMonitor;
    }

    public static String getAppName() {
        assertInitialized();
        return initializedContext.appName;
    }

    //Version if the core library
    public static String getVersion() {
        return "r3.7.2-BETA-4";
    }

    private static void assertInitialized() {
        if (initializedContext == null) {
            throw new IllegalStateException("EnigmaRiverContext is not initialized.");
        }
    }

    public static class EnigmaRiverContextInitialization {
        private IHttpHandler httpHandler = null;
        private String exposureBaseUrl = null;
        private String analyticsUrl = null;
        private String appName = "";
        private IDeviceInfo deviceInfo = null;
        private IActivityLifecycleManagerFactory activityLifecycleManagerFactory = new DefaultActivityLifecycleManagerFactory();
        private ITaskFactoryProvider taskFactoryProvider = new DefaultTaskFactoryProvider(new DefaultTaskFactory());
        private IEpgLocator epgLocator = new DefaultEpgLocator();
        private INetworkMonitor networkMonitor = new DefaultNetworkMonitor();
        private final Map<String, IModuleInitializationSettings> moduleSettings = new HashMap<>();
        private IAdInsertionFactory adInsertionFactory;
        private DeviceParameters deviceParameters;
        private EnigmaStorageManager storageManager;

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

        public IHttpHandler getHttpHandler() {
            if (httpHandler == null) {
                httpHandler = new DefaultHttpHandler();
            }

            return httpHandler;
        }

        public IAdInsertionFactory getAdInsertionFactory() {
            return adInsertionFactory;
        }

        public EnigmaRiverContextInitialization setHttpHandler(final IHttpHandler httpHandler) {
            this.httpHandler = httpHandler;
            return this;
        }

        public IDeviceInfo getDeviceInfo(final Application application) {
            if (deviceInfo != null) {
                return deviceInfo;
            } else {
                return new DeviceInfo(application);
            }
        }

        public EnigmaRiverContextInitialization setDeviceInfo(final IDeviceInfo deviceInfo) {
            this.deviceInfo = deviceInfo;
            return this;
        }

        public EnigmaRiverContextInitialization setAppName(final String appName) {
            this.appName = appName;
            return this;
        }

        public EnigmaRiverContextInitialization setAdInsertionFactory(IAdInsertionFactory adInsertionFactory) {
            this.adInsertionFactory = adInsertionFactory;
            return this;
        }

        public EnigmaRiverContextInitialization setDeviceParameters(DeviceParameters parameters) {
            this.deviceParameters = parameters;
            return this;
        }

        public EnigmaRiverContextInitialization setActivityLifecycleManagerFactory(IActivityLifecycleManagerFactory activityLifecycleManagerFactory) {
            this.activityLifecycleManagerFactory = activityLifecycleManagerFactory;
            return this;
        }

        public DeviceParameters getDeviceParameters() {
            return deviceParameters;
        }

        public String getAppName() {
            return this.appName;
        }

        public IActivityLifecycleManager getActivityLifecycleManager(Application application) {
            return activityLifecycleManagerFactory.createActivityLifecycleManager(application);
        }

        public ITaskFactory getTaskFactory() {
            return taskFactoryProvider.getTaskFactory();
        }

        protected EnigmaRiverContextInitialization setTaskFactory(ITaskFactory taskFactory) {
            if (taskFactoryProvider instanceof DefaultTaskFactoryProvider) {
                ((DefaultTaskFactoryProvider) taskFactoryProvider).setTaskFactory(taskFactory);
            } else {
                throw new IllegalStateException("Custom ITaskFactoryProvider has been set. Use this to set the TaskFactory");
            }
            return this;
        }

        public IEpgLocator getEpgLocator() {
            return epgLocator;
        }

        public EnigmaRiverContextInitialization setEpgLocator(IEpgLocator epgLocator) {
            this.epgLocator = epgLocator;
            return this;
        }

        public ITaskFactoryProvider getTaskFactoryProvider() {
            return taskFactoryProvider;
        }

        public EnigmaRiverContextInitialization setTaskFactoryProvider(ITaskFactoryProvider taskFactoryProvider) {
            this.taskFactoryProvider = taskFactoryProvider;
            return this;
        }

        public INetworkMonitor getNetworkMonitor() {
            return networkMonitor;
        }

        public EnigmaRiverContextInitialization setNetworkMonitor(INetworkMonitor networkMonitor) {
            this.networkMonitor = networkMonitor;
            return this;
        }

        public <I extends IModuleInitializationSettings> I forModule(IModuleInfo<I> moduleInfo) {
            String moduleId = moduleInfo.getModuleId();
            I moduleInitialization = (I) moduleSettings.get(moduleId);
            if (moduleInitialization == null) {
                moduleInitialization = moduleInfo.createInitializationSettings();
                moduleSettings.put(moduleId, moduleInitialization);
            }
            return moduleInitialization;
        }

        public String getAnalyticsUrl() {
            return analyticsUrl;
        }

        public EnigmaRiverContextInitialization setAnalyticsUrl(String analyticsUrl) {
            this.analyticsUrl = analyticsUrl;
            return this;
        }

        public EnigmaStorageManager getStorageManager(Application application) {
            if (storageManager == null) {
                storageManager = new EnigmaStorageManager(application);
            }
            return storageManager;
        }

        public EnigmaRiverContextInitialization setStorageManager(EnigmaStorageManager storageManager) {
            this.storageManager = storageManager;
            return this;
        }
    }

    private static class EnigmaRiverInitializedContext {
        // it can be re-initialized
        private UrlPath exposureBaseUrl;
        private EnigmaStorageManager storageManager;
        // it can be re-initialized
        private UrlPath analyticsUrl;
        // it can be re-initialized
        private String appName;
        // it can be re-initialized
        private IDeviceInfo deviceInfo;
        // it can be re-initialized
        private IAdInsertionFactory adInsertionFactory;
        private final IHttpHandler httpHandler;
        private final IActivityLifecycleManager activityLifecycleManager;
        private final ITaskFactoryProvider taskFactoryProvider;
        private final IEpgLocator epgLocator;
        private final INetworkMonitor networkMonitor;
        private final DeviceParameters deviceParameters;

        public EnigmaRiverInitializedContext(Application application, EnigmaRiverContextInitialization initialization) {
            try {
                String baseUrl = initialization.getExposureBaseUrl();
                if (baseUrl == null) {
                    throw new IllegalStateException("No exposure base url supplied.");
                }
                this.exposureBaseUrl = new UrlPath(baseUrl);
                String analyticsUrl = initialization.getAnalyticsUrl();
                this.analyticsUrl = analyticsUrl != null ? new UrlPath(analyticsUrl) : null;
                this.httpHandler = initialization.getHttpHandler();
                this.deviceInfo = initialization.getDeviceInfo(application);
                this.appName = initialization.appName;
                this.activityLifecycleManager = initialization.getActivityLifecycleManager(application);
                this.taskFactoryProvider = initialization.getTaskFactoryProvider();
                this.epgLocator = initialization.getEpgLocator();
                this.storageManager = initialization.getStorageManager(application);
                if (initialization.getDeviceParameters() == null) {
                    this.deviceParameters = new DeviceParameters();
                } else {
                    this.deviceParameters = initialization.getDeviceParameters();
                }
                this.networkMonitor = initialization.getNetworkMonitor();
                if (networkMonitor instanceof IDefaultNetworkMonitor) {
                    ((IDefaultNetworkMonitor) networkMonitor).start(application.getApplicationContext(), taskFactoryProvider);
                }
                this.adInsertionFactory = initialization.getAdInsertionFactory();
                ProcessLifecycleHandler.get().initialize(application);
            } catch (Exception e) {
                throw new ContextInitializationException(e);
            }
        }

        public DeviceParameters getDeviceParameters() {
            return deviceParameters;
        }
    }
}
