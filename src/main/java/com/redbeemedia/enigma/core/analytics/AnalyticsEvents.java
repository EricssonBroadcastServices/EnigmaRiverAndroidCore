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
    public static final AnalyticsAppBackgroundedEvent APP_BACKGROUNDED = new AnalyticsAppBackgroundedEvent();
    public static final AnalyticsAppResumedEvent APP_RESUMED = new AnalyticsAppResumedEvent();
    public static final AnalyticsGracePeriodEndedEvent GRACE_PERIOD_ENDED = new AnalyticsGracePeriodEndedEvent();
    public static final AnalyticsBitrateChangedEvent BITRATE_CHANGED = new AnalyticsBitrateChangedEvent();
    public static final AnalyticsBufferingStartedEvent BUFFERING_STARTED = new AnalyticsBufferingStartedEvent();
    public static final AnalyticsBufferingStoppedEvent BUFFERING_STOPPED = new AnalyticsBufferingStoppedEvent();
    public static final AnalyticsScrubbedToEvent SCRUBBED_TO = new AnalyticsScrubbedToEvent();
    public static final AnalyticsProgramChangedEvent PROGRAM_CHANGED = new AnalyticsProgramChangedEvent();
    public static final AnalyticsAdEvent AD_STARTED = new AnalyticsAdEvent("Playback.AdStarted");
    public static final AnalyticsAdEvent AD_COMPLETED = new AnalyticsAdEvent("Playback.AdCompleted");
    public static final AnalyticsDrm DRM = new AnalyticsDrm();
    public static final AnalyticsStartCasting START_CASTING = new AnalyticsStartCasting();
    public static final AnalyticsStopCasting STOP_CASTING = new AnalyticsStopCasting();
    public static final AnalyticsAdFailed AD_FAILED = new AnalyticsAdFailed();

    private static <E extends IAnalyticsEventType,T> IEventProperty<E,T> mandatory(String name) {
        return EventProperty.newMandatoryProperty(name);
    }

    private static <E extends IAnalyticsEventType,T> IEventProperty<E,T> optional(String name) {
        return EventProperty.newProperty(name, true);
    }

    public static class AnalyticsErrorEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Error";
        }

        public final IEventProperty<AnalyticsErrorEvent, Integer> CODE = mandatory("Code");
        public final IEventProperty<AnalyticsErrorEvent, String> MESSAGE = mandatory("Message");
        public final IEventProperty<AnalyticsErrorEvent, String> DETAILS = mandatory("Details");
    }


    public static class AnalyticsDeviceInfoEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Device.Info";
        }

        public final IEventProperty<AnalyticsDeviceInfoEvent, String> DEVICE_ID = mandatory("DeviceId");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> DEVICE_MODEL = mandatory("DeviceModel");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> OS = mandatory("OS");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> OS_VERSION = mandatory("OSVersion");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> MANUFACTURER = mandatory("Manufacturer");
        public final IEventProperty<AnalyticsDeviceInfoEvent, Boolean> IS_ROOTED = mandatory("IsRooted");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> WIDEVINE_SECURITY_LEVEL = mandatory("WidevineSecurityLevel");
    }


    public static class AnalyticsCreatedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Created";
        }

        public final IEventProperty<AnalyticsCreatedEvent, String> PLAYER = mandatory("Player");
        public final IEventProperty<AnalyticsCreatedEvent, String> VERSION = mandatory("Version");
        public final IEventProperty<AnalyticsCreatedEvent, String> ASSET_ID = optional("AssetId");
    }

    public static class AnalyticsHandshakeStartedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.HandshakeStarted";
        }
        public final IEventProperty<AnalyticsHandshakeStartedEvent, String> ASSET_ID = mandatory("AssetId");
    }

    public static class AnalyticsPlayerReadyEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.PlayerReady";
        }
        public final IEventProperty<AnalyticsPlayerReadyEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
        public final IEventProperty<AnalyticsPlayerReadyEvent, String> TECHNOLOGY = mandatory("Technology");
        public final IEventProperty<AnalyticsPlayerReadyEvent, String> TECH_VERSION = mandatory("TechVersion");
    }

    public static class AnalyticsStartedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Started";
        }
        public final IEventProperty<AnalyticsStartedEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
        public final IEventProperty<AnalyticsStartedEvent, String> PLAY_MODE = mandatory("PlayMode");
        public final IEventProperty<AnalyticsStartedEvent, String> MEDIA_LOCATOR = mandatory("MediaLocator");
        public final IEventProperty<AnalyticsStartedEvent, Long> REFERENCE_TIME = optional("ReferenceTime");
        public final IEventProperty<AnalyticsStartedEvent, Integer> BITRATE = optional("Bitrate");
        public final IEventProperty<AnalyticsStartedEvent, String> PROGRAM_ID = optional("ProgramId");
    }

    public static class AnalyticsPausedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Paused";
        }
        public final IEventProperty<AnalyticsPausedEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
    }

    public static class AnalyticsResumedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Resumed";
        }
        public final IEventProperty<AnalyticsResumedEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
    }

    public static class AnalyticsCompletedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Completed";
        }
        public final IEventProperty<AnalyticsCompletedEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
    }

    public static class AnalyticsAbortedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Aborted";
        }
        public final IEventProperty<AnalyticsAbortedEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
    }

    public static class AnalyticsHeartbeatEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Heartbeat";
        }
        public final IEventProperty<AnalyticsHeartbeatEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
    }


    public static class AnalyticsAppBackgroundedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.AppBackgrounded";
        }
        public final IEventProperty<AnalyticsAppBackgroundedEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
    }

    public static class AnalyticsAppResumedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.AppResumed";
        }
        public final IEventProperty<AnalyticsAppResumedEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
    }

    public static class AnalyticsGracePeriodEndedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.GracePeriodEnded";
        }
        public final IEventProperty<AnalyticsGracePeriodEndedEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
    }

    public static boolean isTerminal(IAnalyticsEventType eventType) {
        return     eventType instanceof AnalyticsErrorEvent
                || eventType instanceof  AnalyticsCompletedEvent
                || eventType instanceof  AnalyticsAbortedEvent
                || eventType instanceof  AnalyticsGracePeriodEndedEvent
                ;
    }

    /*package-protected*/ static abstract class AbstractOffsetTimeAnalyticsEvent implements IAnalyticsEventType {
        public final IEventProperty<AbstractOffsetTimeAnalyticsEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
    }

    public static class AnalyticsBitrateChangedEvent extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsBitrateChangedEvent, Integer> BITRATE = mandatory("Bitrate");
        @Override
        public String getName() {
            return "Playback.BitrateChanged";
        }
    }

    public static class AnalyticsBufferingStartedEvent extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.BufferingStarted";
        }
    }

    public static class AnalyticsBufferingStoppedEvent extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.BufferingStopped";
        }
    }

    public static class AnalyticsScrubbedToEvent extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.ScrubbedTo";
        }
    }

    public static class AnalyticsProgramChangedEvent extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsProgramChangedEvent, String> PROGRAM_ID = mandatory("ProgramId");

        @Override
        public String getName() {
            return "Playback.ProgramChanged";
        }
    }

    public static class AnalyticsAdEvent implements IAnalyticsEventType {
        private final String eventName;
        AnalyticsAdEvent(String eventName) { this.eventName = eventName; }

        @Override
        public String getName() {
            return eventName;
        }
        public final IEventProperty<AnalyticsAdEvent, Long> OFFSET_TIME = mandatory("OffsetTime");
        public final IEventProperty<AnalyticsAdEvent, String> AD_MEDIA_ID = mandatory("AdMediaId");
    }

    public static class AnalyticsDrm extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.DRM";
        }
    }

    public static class AnalyticsStartCasting extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.StartCasting";
        }
    }

    public static class AnalyticsStopCasting extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.StopCasting";
        }
    }

    public static class AnalyticsAdFailed extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.AdFailed";
        }
    }
}
