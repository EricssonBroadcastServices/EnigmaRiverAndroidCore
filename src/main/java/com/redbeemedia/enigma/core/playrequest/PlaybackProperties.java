package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.format.IMediaFormatSelector;
import com.redbeemedia.enigma.core.format.SimpleMediaFormatSelector;


public final class PlaybackProperties implements IPlaybackProperties {
    private PlayFrom playFrom;
    private IMediaFormatSelector mediaFormatSelector = null;

    public PlaybackProperties() {
        this.playFrom = PlayFrom.PLAYER_DEFAULT;
    }

    @Override
    public PlayFrom getPlayFrom() {
        return playFrom;
    }

    public PlaybackProperties setPlayFrom(PlayFrom playFrom) {
        if(playFrom == null) {
            throw new NullPointerException();
        }
        this.playFrom = playFrom;
        return this;
    }

    @Override
    public IMediaFormatSelector getMediaFormatSelector() {
        return mediaFormatSelector;
    }

    public PlaybackProperties setMediaFormatSelector(IMediaFormatSelector mediaFormatSelector) {
        this.mediaFormatSelector = mediaFormatSelector;
        return this;
    }

    public PlaybackProperties setMediaFormatPreference(EnigmaMediaFormat ... enigmaMediaFormats) {
        return setMediaFormatSelector(new SimpleMediaFormatSelector(enigmaMediaFormats));
    }

    @Override
    public int hashCode() {
        return playFrom.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlaybackProperties && equals((PlaybackProperties) obj);
    }

    private boolean equals(PlaybackProperties obj) {
        return obj.playFrom == this.playFrom;
    }
}
