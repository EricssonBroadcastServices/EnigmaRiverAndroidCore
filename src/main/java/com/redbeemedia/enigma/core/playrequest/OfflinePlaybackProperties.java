// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.playrequest;

/**
 * Use this class to pass offline playback properties
 */
public final class OfflinePlaybackProperties extends PlaybackProperties {

    public OfflinePlaybackProperties() {
        this.playFrom = PlayFrom.BEGINNING;
    }
}