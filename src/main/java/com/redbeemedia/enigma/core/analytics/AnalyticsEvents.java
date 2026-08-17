// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

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
    public static final AnalyticsAdBreakFinished AD_BREAK_FINISHED = new AnalyticsAdBreakFinished();
    public static final AnalyticsAdBreakStarted AD_BREAK_STARTED = new AnalyticsAdBreakStarted();
    public static final AnalyticsAdSkipped AD_SKIPPED = new AnalyticsAdSkipped();
    public static final AnalyticsAudioChanged AUDIO_CHANGED = new AnalyticsAudioChanged();
    public static final AnalyticsAudioPlaybackQualityChanged AUDIO_PLAYBACK_QUALITY_CHANGED = new AnalyticsAudioPlaybackQualityChanged();
    public static final AnalyticsCastStart CAST_START = new AnalyticsCastStart();
    public static final AnalyticsDVRWindowExceeded DVR_WINDOW_EXCEEDED = new AnalyticsDVRWindowExceeded();
    public static final AnalyticsDurationChanged DURATION_CHANGED = new AnalyticsDurationChanged();
    public static final AnalyticsMuted MUTED = new AnalyticsMuted();
    public static final AnalyticsPlay PLAY = new AnalyticsPlay();
    public static final AnalyticsSeeked SEEKED = new AnalyticsSeeked();
    public static final AnalyticsSourceLoaded SOURCE_LOADED = new AnalyticsSourceLoaded();
    public static final AnalyticsTimeChanged TIME_CHANGED = new AnalyticsTimeChanged();
    public static final AnalyticsTimeShifted TIME_SHIFTED = new AnalyticsTimeShifted();
    public static final AnalyticsUnmuted UNMUTED = new AnalyticsUnmuted();
    public static final AnalyticsWarning WARNING = new AnalyticsWarning();
    public static final AnalyticsConnectionTypeChange CONNECTION_TYPE_CHANGE = new AnalyticsConnectionTypeChange();

    private static <E extends IAnalyticsEventType, T> IEventProperty<E, T> mandatory(String name) {
        return EventProperty.newMandatoryProperty(name);
    }

    private static <E extends IAnalyticsEventType, T> IEventProperty<E, T> optional(String name) {
        return EventProperty.newProperty(name, true);
    }

    public static <E extends IAnalyticsEventType> IEventProperty<E, String> playerTechnology() {
        return optional("PlayerTechnology");
    }

    public static class AnalyticsErrorEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Error";
        }

        public final IEventProperty<AnalyticsErrorEvent, Integer> CODE = mandatory("Code");
        public final IEventProperty<AnalyticsErrorEvent, String> MESSAGE = mandatory("Message");
        public final IEventProperty<AnalyticsErrorEvent, String> DETAILS = mandatory("Details");
        public final IEventProperty<AnalyticsErrorEvent, String> UTC_TIME_DIFFERENCE = optional(
                "deviceUTCtimedifference");
    }

    public static class AnalyticsDeviceInfoEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Device.Info";
        }

        public final IEventProperty<AnalyticsDeviceInfoEvent, String> DEVICE_ID = mandatory("DeviceId");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> DEVICE_MODEL = mandatory("DeviceModel");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> OS = mandatory("OS");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> CDN_VENDOR = mandatory("CDNVendor");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> OS_VERSION = mandatory("OSVersion");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> MANUFACTURER = mandatory("Manufacturer");
        public final IEventProperty<AnalyticsDeviceInfoEvent, Boolean> IS_ROOTED = mandatory("IsRooted");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> VERSION = mandatory("Version");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> SDK_VERSION = mandatory("SdkVersion");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> TECHNOLOGY = mandatory("Technology");
        public final IEventProperty<AnalyticsDeviceInfoEvent, String> WIDEVINE_SECURITY_LEVEL = mandatory(
                "WidevineSecurityLevel");
    }

    public static class AnalyticsCreatedEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Created";
        }

        public final IEventProperty<AnalyticsCreatedEvent, String> PLAYER = mandatory("Player");
        public final IEventProperty<AnalyticsCreatedEvent, String> VERSION = mandatory("Version");
        public final IEventProperty<AnalyticsCreatedEvent, String> ASSET_ID = optional("AssetId");
        public final IEventProperty<AnalyticsCreatedEvent, String> APP_TYPE = mandatory("AppType");
        public final IEventProperty<AnalyticsCreatedEvent, String> APP_NAME = mandatory("AppName");
        public final IEventProperty<AnalyticsCreatedEvent, String> APP_VERSION = mandatory("AppVersion");
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
        public final IEventProperty<AnalyticsStartedEvent, Integer> VIDEO_LENGTH = optional("VideoLength");
        public final IEventProperty<AnalyticsStartedEvent, String> PROGRAM_ID = optional("ProgramId");
        public final IEventProperty<AnalyticsStartedEvent, String> DRM_LICENCSE_DURATION_REMAINING_TIME = optional(
                "drmLicenseDurationRemainingTime");
        public final IEventProperty<AnalyticsStartedEvent, String> DRM_RENEW_ALLOWED = optional("drmRenewAllowed");
        public final IEventProperty<AnalyticsStartedEvent, String> DRM_PLAYBACK_DURATION_REMAINING = optional(
                "drmPlaybackDurationRemaining");
        public final IEventProperty<AnalyticsStartedEvent, String> DRM_LICENSE_TYPE = optional("drmLicenseType");
        public final IEventProperty<AnalyticsStartedEvent, String> DRM_PLAY_ALLOWED = optional("drmPlayAllowed");
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
        return eventType instanceof AnalyticsErrorEvent
                || eventType instanceof AnalyticsCompletedEvent
                || eventType instanceof AnalyticsAbortedEvent
                || eventType instanceof AnalyticsGracePeriodEndedEvent;
    }

    /* package-protected */ static abstract class AbstractOffsetTimeAnalyticsEvent implements IAnalyticsEventType {
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
        public final IEventProperty<AnalyticsProgramChangedEvent, String> PROGRAM_ASSET_ID = mandatory(
                "ProgramAssetId");

        @Override
        public String getName() {
            return "Playback.ProgramChanged";
        }
    }

    public static class AnalyticsAdEvent implements IAnalyticsEventType {
        private final String eventName;

        AnalyticsAdEvent(String eventName) {
            this.eventName = eventName;
        }

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

    public static class AnalyticsAdBreakFinished extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsAdBreakFinished, String> AD_BREAK_ID = optional("AdBreakId");

        @Override
        public String getName() {
            return "Playback.AdBreakFinished";
        }
    }

    public static class AnalyticsAdBreakStarted extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsAdBreakStarted, String> AD_BREAK_ID = optional("AdBreakId");

        @Override
        public String getName() {
            return "Playback.AdBreakStarted";
        }
    }

    public static class AnalyticsAdSkipped extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsAdSkipped, String> AD_MEDIA_ID = optional("AdMediaId");

        @Override
        public String getName() {
            return "Playback.AdSkipped";
        }
    }

    public static class AnalyticsAudioChanged extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsAudioChanged, String> AUDIO_TRACK_ID = optional("AudioTrackId");

        @Override
        public String getName() {
            return "Playback.AudioChanged";
        }
    }

    public static class AnalyticsAudioPlaybackQualityChanged extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsAudioPlaybackQualityChanged, Integer> BITRATE = optional("Bitrate");

        @Override
        public String getName() {
            return "Playback.AudioPlaybackQualityChanged";
        }
    }

    public static class AnalyticsCastStart extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.CastStart";
        }
    }

    public static class AnalyticsDVRWindowExceeded extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.DVRWindowExceeded";
        }
    }

    public static class AnalyticsDurationChanged extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsDurationChanged, Double> DURATION = optional("Duration");

        @Override
        public String getName() {
            return "Playback.DurationChanged";
        }
    }

    public static class AnalyticsMuted extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.Muted";
        }
    }

    public static class AnalyticsPlay extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsPlay, Double> TIME = optional("Time");

        @Override
        public String getName() {
            return "Playback.Play";
        }
    }

    public static class AnalyticsSeeked extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.Seeked";
        }
    }

    public static class AnalyticsSourceLoaded extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.SourceLoaded";
        }
    }

    public static class AnalyticsTimeChanged extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsTimeChanged, Double> TIME = optional("Time");

        @Override
        public String getName() {
            return "Playback.TimeChanged";
        }
    }

    public static class AnalyticsTimeShifted extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.TimeShifted";
        }
    }

    public static class AnalyticsUnmuted extends AbstractOffsetTimeAnalyticsEvent {
        @Override
        public String getName() {
            return "Playback.Unmuted";
        }
    }

    public static class AnalyticsWarning extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsWarning, Integer> CODE = optional("Code");
        public final IEventProperty<AnalyticsWarning, String> MESSAGE = optional("Message");

        @Override
        public String getName() {
            return "Playback.Warning";
        }
    }

    public static class AnalyticsConnectionTypeChange extends AbstractOffsetTimeAnalyticsEvent {
        public final IEventProperty<AnalyticsConnectionTypeChange, String> CONNECTION = mandatory("Connection");

        @Override
        public String getName() {
            return "Playback.ConnectionTypeChange";
        }
    }
}
