package com.redbeemedia.enigma.core.context;

import android.app.Application;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManagerFactory;
import com.redbeemedia.enigma.core.context.exception.ContextInitializationException;
import com.redbeemedia.enigma.core.epg.IEpgLocator;
import com.redbeemedia.enigma.core.http.DefaultHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.network.IDefaultNetworkMonitor;
import com.redbeemedia.enigma.core.network.INetworkMonitor;
import com.redbeemedia.enigma.core.playrequest.IAdInsertionFactory;
import com.redbeemedia.enigma.core.playrequest.IAdInsertionParameters;
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
            if(application == null) {
                throw new NullPointerException("application was null");
            }
            if(initializedContext == null) {
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

    public static IAdInsertionFactory getAdInsertionFactory() {
        assertInitialized();
        return initializedContext.adInsertionFactory;
    }

    /**
     *
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

    //Version if the core library
    public static String getVersion() {
        String version = "r3.1.8";
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
        private ITaskFactoryProvider taskFactoryProvider = new DefaultTaskFactoryProvider(new DefaultTaskFactory());
        private IEpgLocator epgLocator = new DefaultEpgLocator();
        private INetworkMonitor networkMonitor = new DefaultNetworkMonitor();
        private final Map<String, IModuleInitializationSettings> moduleSettings = new HashMap<>();
        private IAdInsertionFactory adInsertionFactory;

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

        public IAdInsertionFactory getAdInsertionFactory() {
            return adInsertionFactory;
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

        public EnigmaRiverContextInitialization setAdInsertionFactory(IAdInsertionFactory adInsertionFactory) {
            this.adInsertionFactory = adInsertionFactory;
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
            return taskFactoryProvider.getTaskFactory();
        }

        protected EnigmaRiverContextInitialization setTaskFactory(ITaskFactory taskFactory) {
            if(taskFactoryProvider instanceof DefaultTaskFactoryProvider) {
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
            if(moduleInitialization == null) {
                moduleInitialization = moduleInfo.createInitializationSettings();
                moduleSettings.put(moduleId, moduleInitialization);
            }
            return moduleInitialization;
        }
    }

    private static class EnigmaRiverInitializedContext {
        private final UrlPath exposureBaseUrl;
        private final IHttpHandler httpHandler;
        private final IDeviceInfo deviceInfo;
        private final IActivityLifecycleManager activityLifecycleManager;
        private final ITaskFactoryProvider taskFactoryProvider;
        private final IEpgLocator epgLocator;
        private final INetworkMonitor networkMonitor;
        private final IAdInsertionFactory adInsertionFactory;

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
                this.taskFactoryProvider = initialization.getTaskFactoryProvider();
                this.epgLocator = initialization.getEpgLocator();
                this.networkMonitor = initialization.getNetworkMonitor();
                if(networkMonitor instanceof IDefaultNetworkMonitor) {
                    ((IDefaultNetworkMonitor) networkMonitor).start(application.getApplicationContext(), taskFactoryProvider);
                }
                this.adInsertionFactory = initialization.getAdInsertionFactory();
                ProcessLifecycleHandler.get().initialize(application);
            } catch (Exception e) {
                throw new ContextInitializationException(e);
            }
        }
    }
}
