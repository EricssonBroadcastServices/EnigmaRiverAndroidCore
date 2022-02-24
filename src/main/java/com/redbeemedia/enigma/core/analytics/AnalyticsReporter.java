package com.redbeemedia.enigma.core.analytics;

import android.util.Log;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.PlayerImplementationError;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.util.device.IDeviceInfo;

import org.json.JSONException;

public class AnalyticsReporter implements IAnalyticsReporter {
    private static final String TAG = "AnalyticsReporter";

    private final IAnalyticsHandler analyticsHandler;
    private final ITimeProvider timeProvider;
    private volatile boolean terminalStateReached = false;
    private final long deviceUtcTimeDifference;

    public AnalyticsReporter(ITimeProvider timeProvider, IAnalyticsHandler analyticsHandler, long deviceUtcTimeDifference) {
        this.timeProvider = timeProvider;
        this.analyticsHandler = analyticsHandler;
        this.deviceUtcTimeDifference = deviceUtcTimeDifference;
    }

    @Override
    public void playbackError(EnigmaError error) {
        event(AnalyticsEvents.ERROR, (builder, eventType) -> {
            builder.addData(eventType.CODE, error.getErrorCode());
            String additionalMessage = "";

            if (error instanceof PlayerImplementationError) {
                PlayerImplementationError playerImplementationError = (PlayerImplementationError) error;
                builder.addData(new PlayerSpecificErrorCode(playerImplementationError), playerImplementationError.getInternalErrorCode());
            }

            if (!error.getMessageAndCauseMessage().trim().isEmpty()) {
                // will add message and cause message in it
                additionalMessage = " : " + error.getMessageAndCauseMessage();
            }

            builder.addData(eventType.MESSAGE, error.getClass().getSimpleName() + additionalMessage);
            builder.addData(eventType.DETAILS, error.getTrace());
            builder.addData(eventType.UTC_TIME_DIFFERENCE, deviceUtcTimeDifference+"");
        });
    }

    @Override
    public void deviceInfo(String cdnProvider) {
        event(AnalyticsEvents.DEVICE_INFO, (builder, eventType) -> {
            IDeviceInfo deviceInfo = EnigmaRiverContext.getDeviceInfo();

            builder.addData(eventType.DEVICE_ID, deviceInfo.getDeviceId());
            builder.addData(eventType.DEVICE_MODEL, deviceInfo.getModel());
            builder.addData(eventType.OS, deviceInfo.getOS());
            builder.addData(eventType.APP_TYPE, deviceInfo.getAppType());
            builder.addData(eventType.CDN_VENDOR, cdnProvider);
            builder.addData(eventType.OS_VERSION, deviceInfo.getOSVersion());
            builder.addData(eventType.MANUFACTURER, deviceInfo.getManufacturer());
            builder.addData(eventType.IS_ROOTED, deviceInfo.isDeviceRooted());
            builder.addData(eventType.WIDEVINE_SECURITY_LEVEL, deviceInfo.getWidevineDrmSecurityLevel());
        });
    }

    @Override
    public void playbackCreated(String assetId) {
        event(AnalyticsEvents.CREATED, (builder, eventType) -> {
            builder.addData(eventType.PLAYER, "EnigmaRiver.Android");
            builder.addData(eventType.VERSION, EnigmaRiverContext.getVersion());
            builder.addData(eventType.ASSET_ID, assetId);
        });
    }

    @Override
    public void playbackHandshakeStarted(String assetId) {
        event(AnalyticsEvents.HANDSHAKE_STARTED, (builder, eventType) -> {
            builder.addData(eventType.ASSET_ID, assetId);
        });
    }

    @Override
    public void playbackPlayerReady(long offsetTime, String playerImplementationTechnology, String playerImplementationTechnologyVersion) {
        event(AnalyticsEvents.PLAYER_READY, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.TECHNOLOGY, playerImplementationTechnology);
            builder.addData(eventType.TECH_VERSION, playerImplementationTechnologyVersion);
        });
    }

    @Override
    public void playbackStarted(long offsetTime, String playMode, String mediaLocator, Long referenceTime, Integer bitrate, String programId) {
        event(AnalyticsEvents.STARTED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.PLAY_MODE, playMode);
            builder.addData(eventType.MEDIA_LOCATOR, mediaLocator);
            builder.addData(eventType.REFERENCE_TIME, referenceTime);
            builder.addData(eventType.BITRATE, bitrate);
            builder.addData(eventType.PROGRAM_ID, programId);
        });
    }

    @Override
    public void playbackPaused(long offsetTime) {
        event(AnalyticsEvents.PAUSED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackResumed(long offsetTime) {
        event(AnalyticsEvents.RESUMED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackCompleted(long offsetTime) {
        event(AnalyticsEvents.COMPLETED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackAborted(long offsetTime) {
        event(AnalyticsEvents.ABORTED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackHeartbeat(long offsetTime) {
        event(AnalyticsEvents.HEARTBEAT, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackAppBackgrounded(long offsetTime) {
        event(AnalyticsEvents.APP_BACKGROUNDED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackAppResumed(long offsetTime) {
        event(AnalyticsEvents.APP_RESUMED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackGracePeriodEnded(long offsetTime) {
        event(AnalyticsEvents.GRACE_PERIOD_ENDED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackBitrateChanged(long offsetTime, int kilobitsPerSecond) {
        event(AnalyticsEvents.BITRATE_CHANGED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.BITRATE, kilobitsPerSecond);
        });
    }

    @Override
    public void playbackBufferingStarted(long offsetTime) {
        event(AnalyticsEvents.BUFFERING_STARTED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackBufferingStopped(long offsetTime) {
        event(AnalyticsEvents.BUFFERING_STOPPED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackScrubbedTo(long offsetTime) {
        event(AnalyticsEvents.SCRUBBED_TO, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackProgramChanged(long offsetTime, String programId) {
        event(AnalyticsEvents.PROGRAM_CHANGED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.PROGRAM_ID, programId);
        });
    }

    @Override
    public void playbackAdStarted(long offsetTime, String adId) {
        event(AnalyticsEvents.AD_STARTED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.AD_MEDIA_ID, adId);
        });
    }

    @Override
    public void playbackAdCompleted(long offsetTime, String adId) {
        event(AnalyticsEvents.AD_COMPLETED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.AD_MEDIA_ID, adId);
        });
    }

    @Override
    public void playbackDrm(long offsetTime) {
        event(AnalyticsEvents.DRM, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }
    @Override
    public void playbackStartCasting(long offsetTime) {
        event(AnalyticsEvents.START_CASTING, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackStopCasting(long offsetTime) {
        event(AnalyticsEvents.STOP_CASTING, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackAdFailed(long offsetTime) {
        event(AnalyticsEvents.AD_FAILED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    private interface IEventConstruction<T extends IAnalyticsEventType> {
        void construct(IAnalyticsEventBuilder<T> builder, T eventType) throws Exception;
    }

    private <T extends IAnalyticsEventType> void event(T eventType, IEventConstruction<T> construction) {
        try {
            if(!terminalStateReached) {
                IAnalyticsEventBuilder<T> builder = newEventBuilder(eventType);
                construction.construct(builder, eventType);
                analyticsHandler.onAnalytics(builder.build());
                if(AnalyticsEvents.isTerminal(eventType)) {
                    terminalStateReached = true;
                }
            } else {
                Log.d(TAG, "Skipping "+eventType.getName()+" after terminal event");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        if(BuildConfig.DEBUG) {
            throw new RuntimeException(e);
        } else {
            e.printStackTrace();
        }
    }

    protected <E extends IAnalyticsEventType> IAnalyticsEventBuilder<E> newEventBuilder(E eventType) throws JSONException {
        return new JsonAnalyticsEventBuilder(eventType.getName(), timeProvider.getTime());
    }

    private static class PlayerSpecificErrorCode implements IEventProperty<AnalyticsEvents.AnalyticsErrorEvent, Integer> {
        private final String name;

        public PlayerSpecificErrorCode(PlayerImplementationError playerImplementationError) {
            this.name = playerImplementationError.getInternalErrorCodeFieldName();
        }

        @Override
        public boolean skipIfNull() {
            return false;
        }

        @Override
        public boolean isMandatory() {
            return false;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
