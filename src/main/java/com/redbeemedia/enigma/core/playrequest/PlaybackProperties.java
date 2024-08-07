// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.playrequest;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.format.IMediaFormatSelector;
import com.redbeemedia.enigma.core.format.SimpleMediaFormatSelector;

public class PlaybackProperties implements IPlaybackProperties {
    protected PlayFrom playFrom;
    private IMediaFormatSelector mediaFormatSelector = null;
    private AdobePrimetime primetimeToken;
    private boolean analyticsEnabled = true;
    private MaterialProfile materialProfile;

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
    @Nullable public IMediaFormatSelector getMediaFormatSelector() {
        return mediaFormatSelector;
    }

    @Override
    public MaterialProfile getMaterialProfile() {
        return this.materialProfile;
    }

    public void setMaterialProfile(MaterialProfile materialProfile) {
        this.materialProfile = materialProfile;
    }

    @Nullable
    @Override
    public AdobePrimetime getAdobePrimetime() {
        return primetimeToken;
    }

    @Override
    public boolean enableAnalytics() {
        return analyticsEnabled;
    }

    public PlaybackProperties setMediaFormatSelector(IMediaFormatSelector mediaFormatSelector) {
        this.mediaFormatSelector = mediaFormatSelector;
        return this;
    }

    public PlaybackProperties setAdobePrimetime(@Nullable AdobePrimetime primetimeToken) {
        this.primetimeToken = primetimeToken;
        return this;
    }

    public PlaybackProperties setAnalyticsEnabled(boolean enabled) {
        this.analyticsEnabled = enabled;
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


