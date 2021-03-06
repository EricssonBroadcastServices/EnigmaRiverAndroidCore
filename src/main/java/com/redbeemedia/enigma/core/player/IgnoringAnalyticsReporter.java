package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.analytics.IAnalyticsReporter;
import com.redbeemedia.enigma.core.error.EnigmaError;

/*package-protected*/ class IgnoringAnalyticsReporter implements IAnalyticsReporter {
    @Override
    public void playbackError(EnigmaError error) {

    }

    @Override
    public void deviceInfo() {

    }

    @Override
    public void playbackCreated(String assetId) {

    }

    @Override
    public void playbackHandshakeStarted(String assetId) {

    }

    @Override
    public void playbackPlayerReady(long offsetTime, String playerImplementationTechnology, String playerImplementationTechnologyVersion) {

    }

    @Override
    public void playbackStarted(long offsetTime, String playMode, String mediaLocator, Long referenceTime, Integer bitrate, String programId) {

    }

    @Override
    public void playbackPaused(long offsetTime) {

    }

    @Override
    public void playbackResumed(long offsetTime) {

    }

    @Override
    public void playbackCompleted(long offsetTime) {

    }

    @Override
    public void playbackAborted(long offsetTime) {

    }

    @Override
    public void playbackHeartbeat(long offsetTime) {

    }

    @Override
    public void playbackAppBackgrounded(long offsetTime) {

    }

    @Override
    public void playbackAppResumed(long offsetTime) {

    }

    @Override
    public void playbackGracePeriodEnded(long offsetTime) {

    }

    @Override
    public void playbackBitrateChanged(long offsetTime, int kilobitsPerSecond) {

    }

    @Override
    public void playbackBufferingStarted(long offsetTime) {

    }

    @Override
    public void playbackBufferingStopped(long offsetTime) {

    }

    @Override
    public void playbackScrubbedTo(long offsetTime) {

    }

    @Override
    public void playbackProgramChanged(long offsetTime, String programId) {

    }
}
