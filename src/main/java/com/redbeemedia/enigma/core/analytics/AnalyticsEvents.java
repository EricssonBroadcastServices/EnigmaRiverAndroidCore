package com.redbeemedia.enigma.core.analytics;

/*package-protected*/ class AnalyticsEvents {
    public static final AnalyticsErrorEvent ERROR = new AnalyticsErrorEvent();
    public static final AnalyticsDeviceInfoEvent DEVICE_INFO = new AnalyticsDeviceInfoEvent();
    public static final AnalyticsCreatedEvent CREATED = new AnalyticsCreatedEvent();
    public static final AnalyticsHandshakeStartedEvent HANDSHAKE_STARTED = new AnalyticsHandshakeStartedEvent();
    public static final AnalyticsPlayerReadyEvent PLAYER_READY = new AnalyticsPlayerReadyEvent();
    public static final AnalyticsStartedEvent STARTED = new AnalyticsStartedEvent();
    public static final AnalyticsPausedEvent PAUSED = new AnalyticsPausedEvent();
    public static final AnalyticsResumedEvent RESUMED = new AnalyticsResumedEvent();
    public static final AnalyticsCompletedEvent COMPLETED = new AnalyticsCompletedEvent();
    public static final AnalyticsAbortedEvent ABORTED = new AnalyticsAbortedEvent();
    public static final AnalyticsHeartbeatEvent HEARTBEAT = new AnalyticsHeartbeatEvent();

    public static class AnalyticsErrorEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Error";
        }

        public final EventProperty<AnalyticsErrorEvent, Integer> CODE = new EventProperty<>("Code");
        public final EventProperty<AnalyticsErrorEvent, String> MESSAGE = new EventProperty<>("Message");
        public final EventProperty<AnalyticsErrorEvent, String> DETAILS = new EventProperty<>("Details");
    }


    public static class AnalyticsDeviceInfoEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Device.Info";
        }

        public final EventProperty<AnalyticsDeviceInfoEvent, String> DEVICE_ID = new EventProperty<>("DeviceId");
        public final EventProperty<AnalyticsDeviceInfoEvent, String> DEVICE_MODEL = new EventProperty<>("DeviceModel");
        public final EventProperty<AnalyticsDeviceInfoEvent, String> OS = new EventProperty<>("OS");
        public final EventProperty<AnalyticsDeviceInfoEvent, String> OS_VERSION = new EventProperty<>("OSVersion");
        public final EventProperty<AnalyticsDeviceInfoEvent, String> MANUFACTURER = new EventProperty<>("Manufacturer");
        public final EventProperty<AnalyticsDeviceInfoEvent, Boolean> IS_ROOTED = new EventProperty<>("IsRooted");
        public final EventProperty<AnalyticsDeviceInfoEvent, String> WIDEVINE_SECURITY_LEVEL = new EventProperty<>("WidevineSecurityLevel");
    }


    public static class AnalyticsCreatedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Created";
        }

        public final EventProperty<AnalyticsCreatedEvent, String> PLAYER = new EventProperty<>("Player");
        public final EventProperty<AnalyticsCreatedEvent, String> VERSION = new EventProperty<>("Version");
        public final EventProperty<AnalyticsCreatedEvent, String> ASSET_ID = new EventProperty<>("AssetId", true);
    }

    public static class AnalyticsHandshakeStartedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.HandshakeStarted";
        }
        public final EventProperty<AnalyticsHandshakeStartedEvent, String> ASSET_ID = new EventProperty<>("AssetId");
    }

    public static class AnalyticsPlayerReadyEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.PlayerReady";
        }
        public final EventProperty<AnalyticsPlayerReadyEvent, Long> OFFSET_TIME = new EventProperty<>("OffsetTime");
        public final EventProperty<AnalyticsPlayerReadyEvent, String> TECHNOLOGY = new EventProperty<>("Technology");
        public final EventProperty<AnalyticsPlayerReadyEvent, String> TECH_VERSION = new EventProperty<>("TechVersion");
    }

    public static class AnalyticsStartedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Started";
        }
        public final EventProperty<AnalyticsStartedEvent, Long> OFFSET_TIME = new EventProperty<>("OffsetTime");
        public final EventProperty<AnalyticsStartedEvent, String> PLAY_MODE = new EventProperty<>("PlayMode");
        public final EventProperty<AnalyticsStartedEvent, String> MEDIA_LOCATOR = new EventProperty<>("MediaLocator");
        public final EventProperty<AnalyticsStartedEvent, Long> REFERENCE_TIME = new EventProperty<>("ReferenceTime", true);
    }

    public static class AnalyticsPausedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Paused";
        }
        public final EventProperty<AnalyticsPausedEvent, Long> OFFSET_TIME = new EventProperty<>("OffsetTime");
    }

    public static class AnalyticsResumedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Resumed";
        }
        public final EventProperty<AnalyticsResumedEvent, Long> OFFSET_TIME = new EventProperty<>("OffsetTime");
    }

    public static class AnalyticsCompletedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Completed";
        }
        public final EventProperty<AnalyticsCompletedEvent, Long> OFFSET_TIME = new EventProperty<>("OffsetTime");
    }

    public static class AnalyticsAbortedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Aborted";
        }
        public final EventProperty<AnalyticsAbortedEvent, Long> OFFSET_TIME = new EventProperty<>("OffsetTime");
    }

    public static class AnalyticsHeartbeatEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Heartbeat";
        }
        public final EventProperty<AnalyticsHeartbeatEvent, Long> OFFSET_TIME = new EventProperty<>("OffsetTime");
    }

    public static boolean isTerminal(IAnalyticsEventType eventType) {
        return     eventType instanceof AnalyticsErrorEvent
                || eventType instanceof  AnalyticsCompletedEvent
                || eventType instanceof  AnalyticsAbortedEvent;
    }
}
