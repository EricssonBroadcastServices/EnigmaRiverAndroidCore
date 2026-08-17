// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

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
    private volatile String playerTechnology;
    private volatile String sdkVersion;

    public AnalyticsReporter(ITimeProvider timeProvider, IAnalyticsHandler analyticsHandler,
            long deviceUtcTimeDifference) {
        this.timeProvider = timeProvider;
        this.analyticsHandler = analyticsHandler;
        this.deviceUtcTimeDifference = deviceUtcTimeDifference;
    }

    public void setPlayerTechnology(String playerTechnology) {
        this.playerTechnology = playerTechnology;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    @Override
    public void playbackError(EnigmaError error) {
        event(AnalyticsEvents.ERROR, (builder, eventType) -> {
            builder.addData(eventType.CODE, error.getErrorCode());
            String additionalMessage = "";

            if (error instanceof PlayerImplementationError) {
                PlayerImplementationError playerImplementationError = (PlayerImplementationError) error;
                builder.addData(new PlayerSpecificErrorCode(playerImplementationError),
                        playerImplementationError.getInternalErrorCode());
            }

            if (!error.getMessageAndCauseMessage().trim().isEmpty()) {
                // will add message and cause message in it
                additionalMessage = " : " + error.getMessageAndCauseMessage();
            }

            builder.addData(eventType.MESSAGE, error.getClass().getSimpleName() + additionalMessage);
            builder.addData(eventType.DETAILS, error.getTrace());
            builder.addData(eventType.UTC_TIME_DIFFERENCE, deviceUtcTimeDifference + "");
        });
    }

    @Override
    public void deviceInfo(String cdnProvider) {
        deviceInfo(cdnProvider, "ExoPlayer");
    }

    @Override
    public void deviceInfo(String cdnProvider, String technology) {
        event(AnalyticsEvents.DEVICE_INFO, (builder, eventType) -> {
            IDeviceInfo deviceInfo = EnigmaRiverContext.getDeviceInfo();

            builder.addData(eventType.DEVICE_ID, deviceInfo.getDeviceId());
            builder.addData(eventType.DEVICE_MODEL, deviceInfo.getDeviceModelLogin());
            builder.addData(eventType.OS, deviceInfo.getOS());
            builder.addData(eventType.CDN_VENDOR, cdnProvider);
            builder.addData(eventType.OS_VERSION, deviceInfo.getOSVersion());
            builder.addData(eventType.MANUFACTURER, deviceInfo.getManufacturer());
            builder.addData(eventType.IS_ROOTED, deviceInfo.isDeviceRooted());
            builder.addData(eventType.TECHNOLOGY, technology);
            builder.addData(eventType.VERSION, EnigmaRiverContext.getVersion());
            builder.addData(eventType.SDK_VERSION, getSdkVersion());
            builder.addData(eventType.WIDEVINE_SECURITY_LEVEL, deviceInfo.getWidevineDrmSecurityLevel());
        });
    }

    @Override
    public void playbackCreated(String assetId) {
        event(AnalyticsEvents.CREATED, (builder, eventType) -> {
            builder.addData(eventType.PLAYER, "EnigmaRiver.Android");
            builder.addData(eventType.VERSION, EnigmaRiverContext.getVersion());
            builder.addData(eventType.ASSET_ID, assetId);
            builder.addData(eventType.APP_NAME, EnigmaRiverContext.getAppName());
            builder.addData(eventType.APP_VERSION, EnigmaRiverContext.getAppVersion());
            builder.addData(eventType.APP_TYPE, EnigmaRiverContext.getAppType().toString());
        });
    }

    @Override
    public void playbackHandshakeStarted(String assetId) {
        event(AnalyticsEvents.HANDSHAKE_STARTED, (builder, eventType) -> {
            builder.addData(eventType.ASSET_ID, assetId);
        });
    }

    @Override
    public void playbackPlayerReady(long offsetTime, String playerImplementationTechnology,
            String playerImplementationTechnologyVersion) {
        event(AnalyticsEvents.PLAYER_READY, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.TECHNOLOGY, playerImplementationTechnology);
            builder.addData(eventType.TECH_VERSION, playerImplementationTechnologyVersion);
        });
    }

    @Override
    public void playbackStarted(long offsetTime, String playMode, String mediaLocator, Long referenceTime,
            Integer bitrate, String programId, Integer duration,
            String drmLicenseDurationRemainingTime, String drmRenewAllowed, String drmPlaybackDurationRemaining,
            String drmLicenseType, String drmPlayAllowed) {
        event(AnalyticsEvents.STARTED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.PLAY_MODE, playMode);
            builder.addData(eventType.MEDIA_LOCATOR, mediaLocator);
            builder.addData(eventType.REFERENCE_TIME, referenceTime);
            builder.addData(eventType.BITRATE, bitrate);
            builder.addData(eventType.VIDEO_LENGTH, duration);
            builder.addData(eventType.PROGRAM_ID, programId);
            builder.addData(eventType.DRM_LICENCSE_DURATION_REMAINING_TIME, drmLicenseDurationRemainingTime);
            builder.addData(eventType.DRM_RENEW_ALLOWED, drmRenewAllowed);
            builder.addData(eventType.DRM_PLAYBACK_DURATION_REMAINING, drmPlaybackDurationRemaining);
            builder.addData(eventType.DRM_LICENSE_TYPE, drmLicenseType);
            builder.addData(eventType.DRM_PLAY_ALLOWED, drmPlayAllowed);
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
    public void playbackProgramChanged(long offsetTime, String programId, String programAssetId) {
        event(AnalyticsEvents.PROGRAM_CHANGED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.PROGRAM_ID, programId);
            builder.addData(eventType.PROGRAM_ASSET_ID, programAssetId);
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

    @Override
    public void playbackAdBreakFinished(long offsetTime, String adBreakId) {
        event(AnalyticsEvents.AD_BREAK_FINISHED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.AD_BREAK_ID, adBreakId);
        });
    }

    @Override
    public void playbackAdBreakStarted(long offsetTime, String adBreakId) {
        event(AnalyticsEvents.AD_BREAK_STARTED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.AD_BREAK_ID, adBreakId);
        });
    }

    @Override
    public void playbackAdSkipped(long offsetTime, String adId) {
        event(AnalyticsEvents.AD_SKIPPED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.AD_MEDIA_ID, adId);
        });
    }

    @Override
    public void playbackAudioChanged(long offsetTime, String audioTrackId) {
        event(AnalyticsEvents.AUDIO_CHANGED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.AUDIO_TRACK_ID, audioTrackId);
        });
    }

    @Override
    public void playbackAudioPlaybackQualityChanged(long offsetTime, Integer bitrate) {
        event(AnalyticsEvents.AUDIO_PLAYBACK_QUALITY_CHANGED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.BITRATE, bitrate);
        });
    }

    @Override
    public void playbackCastStart(long offsetTime) {
        event(AnalyticsEvents.CAST_START, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackDVRWindowExceeded(long offsetTime) {
        event(AnalyticsEvents.DVR_WINDOW_EXCEEDED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackDurationChanged(long offsetTime, Double duration) {
        event(AnalyticsEvents.DURATION_CHANGED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.DURATION, duration);
        });
    }

    @Override
    public void playbackMuted(long offsetTime) {
        event(AnalyticsEvents.MUTED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackPlay(long offsetTime, Double time) {
        event(AnalyticsEvents.PLAY, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.TIME, time);
        });
    }

    @Override
    public void playbackSeeked(long offsetTime) {
        event(AnalyticsEvents.SEEKED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackSourceLoaded(long offsetTime) {
        event(AnalyticsEvents.SOURCE_LOADED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackTimeChanged(long offsetTime, Double time) {
        event(AnalyticsEvents.TIME_CHANGED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.TIME, time);
        });
    }

    @Override
    public void playbackTimeShifted(long offsetTime) {
        event(AnalyticsEvents.TIME_SHIFTED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackUnmuted(long offsetTime) {
        event(AnalyticsEvents.UNMUTED, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
        });
    }

    @Override
    public void playbackWarning(long offsetTime, Integer code, String message) {
        event(AnalyticsEvents.WARNING, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.CODE, code);
            builder.addData(eventType.MESSAGE, message);
        });
    }

    @Override
    public void playbackConnectionTypeChanged(long offsetTime, String connectionType) {
        event(AnalyticsEvents.CONNECTION_TYPE_CHANGE, (builder, eventType) -> {
            builder.addData(eventType.OFFSET_TIME, offsetTime);
            builder.addData(eventType.CONNECTION, connectionType);
        });
    }

    private interface IEventConstruction<T extends IAnalyticsEventType> {
        void construct(IAnalyticsEventBuilder<T> builder, T eventType) throws Exception;
    }

    private <T extends IAnalyticsEventType> void event(T eventType, IEventConstruction<T> construction) {
        try {
            if (!terminalStateReached) {
                IAnalyticsEventBuilder<T> builder = newEventBuilder(eventType);
                construction.construct(builder, eventType);
                builder.addData(AnalyticsEvents.playerTechnology(), playerTechnology);
                analyticsHandler.onAnalytics(builder.build());
                if (AnalyticsEvents.isTerminal(eventType)) {
                    terminalStateReached = true;
                }
            } else {
                Log.d(TAG, "Skipping " + eventType.getName() + " after terminal event");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private String getSdkVersion() {
        return sdkVersion != null ? sdkVersion : EnigmaRiverContext.getVersion();
    }

    private void handleException(Exception e) {
        if (BuildConfig.DEBUG) {
            throw new RuntimeException(e);
        } else {
            e.printStackTrace();
        }
    }

    protected <E extends IAnalyticsEventType> IAnalyticsEventBuilder<E> newEventBuilder(E eventType)
            throws JSONException {
        return new JsonAnalyticsEventBuilder(eventType.getName(), timeProvider.getTime());
    }

    private static class PlayerSpecificErrorCode
            implements IEventProperty<AnalyticsEvents.AnalyticsErrorEvent, Integer> {
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
