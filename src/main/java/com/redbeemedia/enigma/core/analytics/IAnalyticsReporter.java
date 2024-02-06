package com.redbeemedia.enigma.core.analytics;

import com.redbeemedia.enigma.core.error.EnigmaError;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IAnalyticsReporter {
    void playbackError(EnigmaError error);

    void deviceInfo(String cdnProvider);

    void playbackCreated(String assetId);

    void playbackHandshakeStarted(String assetId);

    void playbackPlayerReady(long offsetTime, String playerImplementationTechnology, String playerImplementationTechnologyVersion);

    void playbackStarted(long offsetTime, String playMode, String mediaLocator, Long referenceTime, Integer bitrate, String programId, Integer duration, String drmLicenseDurationRemainingTime, String drmRenewAllowed, String drmPlaybackDurationRemaining, String drmLicenseType,String drmPlayAllowed);

    void playbackPaused(long offsetTime);

    void playbackResumed(long offsetTime);

    void playbackCompleted(long offsetTime);

    void playbackAborted(long offsetTime);

    void playbackHeartbeat(long offsetTime);

    void playbackAppBackgrounded(long offsetTime);

    void playbackAppResumed(long offsetTime);

    void playbackGracePeriodEnded(long offsetTime);

    void playbackBitrateChanged(long offsetTime, int kilobitsPerSecond);

    void playbackBufferingStarted(long offsetTime);

    void playbackBufferingStopped(long offsetTime);

    void playbackScrubbedTo(long offsetTime);

    void playbackProgramChanged(long offsetTime, String programId, String programAssetId);

    void playbackAdStarted(long offsetTime, String adId);

    void playbackAdCompleted(long offsetTime, String adId);

    void playbackDrm(long offsetTime);

    void playbackStartCasting(long offsetTime);

    void playbackStopCasting(long offsetTime);

    void playbackAdFailed(long offsetTime);
}
