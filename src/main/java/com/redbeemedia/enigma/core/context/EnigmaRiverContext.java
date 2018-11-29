package com.redbeemedia.enigma.core.context;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

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


    public static class EnigmaRiverContextInitialization {
        private IHttpHandler httpHandler = null;
        //TODO remove this default path to prestage exposure.
        private String exposureBaseUrl = "https://psempexposureapi.ebsd.ericsson.net:443";
        private IDeviceInfo deviceInfo = null;

        public String getExposureBaseUrl() {
            return exposureBaseUrl;
        }

        public EnigmaRiverContextInitialization setExposureBaseUrl(String exposureBaseUrl) {
            this.exposureBaseUrl = exposureBaseUrl;
            return this;
        }

        public IHttpHandler getHttpHandler()
        {
            final IHttpHandler handler;
            if(httpHandler == null) {
                handler = new DefaultHttpHandler();
                httpHandler = handler;
            } else {
                handler = httpHandler;
            }

            return handler;
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
    }

    private static class EnigmaRiverInitializedContext {
        private final UrlPath exposureBaseUrl;
        private final IHttpHandler httpHandler;
        private final IDeviceInfo deviceInfo;

        public EnigmaRiverInitializedContext(Application application, EnigmaRiverContextInitialization initialization) {
            try {
                if(application != null) { //Note: Application is only allowed to be null when running unit tests.
                    application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                        @Override
                        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

                        }

                        @Override
                        public void onActivityStarted(Activity activity) {

                        }

                        @Override
                        public void onActivityResumed(Activity activity) {

                        }

                        @Override
                        public void onActivityPaused(Activity activity) {

                        }

                        @Override
                        public void onActivityStopped(Activity activity) {

                        }

                        @Override
                        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                        }

                        @Override
                        public void onActivityDestroyed(Activity activity) {
                            //TODO release proxies
                        }
                    });
                }
                this.exposureBaseUrl = new UrlPath(initialization.getExposureBaseUrl());
                this.httpHandler = initialization.getHttpHandler();
                this.deviceInfo = initialization.getDeviceInfo(application);
            } catch (Exception e) {
                //TODO throw ContextInitializationException
                throw new RuntimeException(e);
            }
        }
    }
}
