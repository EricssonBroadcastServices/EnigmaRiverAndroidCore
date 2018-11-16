package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.http.DefaultHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.util.UrlPath;

public final class EnigmaRiverContext {
    private static volatile EnigmaRiverInitializedContext initializedContext = null;

    public static synchronized void initialize() {
        EnigmaRiverContext.initialize(new EnigmaRiverContextInitialization());
    }

    public static synchronized void initialize(EnigmaRiverContextInitialization initialization) {
        if(initializedContext == null) {
            initializedContext = new EnigmaRiverInitializedContext(initialization);
        } else {
            throw new IllegalStateException("EnigmaRiverContext already initialized.");
        }
    }

    /**
     * For unit tests.
     * @param initialization
     */
    /*package-protected*/ static synchronized void resetInitialization(EnigmaRiverContextInitialization initialization) {
        initializedContext = new EnigmaRiverInitializedContext(initialization);
    }

    public static UrlPath getExposureBaseUrl() {
        //TODO assert initialized
        return initializedContext.exposureBaseUrl;
    }

    public static IHttpHandler getHttpHandler() {
        return initializedContext.httpHandler;
    }

    //    public static IEnigmaPlayer createPlayer(IPlayerImplementation playerImplementation) {
//        return new EnigmaPlayer(playerImplementation);
//    }


    public static class EnigmaRiverContextInitialization {
        private IHttpHandler httpHandler = new DefaultHttpHandler();

        public String getExposureBaseUrl() {
            return "https://psempexposureapi.ebsd.ericsson.net";
        }

        public IHttpHandler getHttpHandler() {
            if(httpHandler == null) {
                throw new NullPointerException();
            }
            return httpHandler;
        }

        public EnigmaRiverContextInitialization setHttpHandler(IHttpHandler httpHandler) {
            this.httpHandler = httpHandler;
            return this;
        }
    }

    private static class EnigmaRiverInitializedContext {
        private final UrlPath exposureBaseUrl;
        private final IHttpHandler httpHandler;

        public EnigmaRiverInitializedContext(EnigmaRiverContextInitialization initialization) {
            try {
                this.exposureBaseUrl = new UrlPath(initialization.getExposureBaseUrl());
                this.httpHandler = initialization.getHttpHandler();
            } catch (Exception e) {
                //TODO throw ContextInitializationException
                throw new RuntimeException(e);
            }
        }
    }
}
