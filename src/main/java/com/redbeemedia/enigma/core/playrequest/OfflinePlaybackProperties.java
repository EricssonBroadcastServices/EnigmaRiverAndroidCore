package com.redbeemedia.enigma.core.playrequest;

/**
 * Use this class to pass offline playback properties
 */
public final class OfflinePlaybackProperties extends PlaybackProperties {

    public OfflinePlaybackProperties() {
        this.playFrom = PlayFrom.BEGINNING;
    }
}