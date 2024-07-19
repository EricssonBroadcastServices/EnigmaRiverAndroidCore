// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;

import java.util.Collection;

/** Represent ad metadata for a playback session. */
public interface IAdMetadata {

    /** Available ad stitcher types. */
    enum AdStitcherType {
        Nowtilus,
        None,
        Unknown
    }

    /** Return type of ad stitcher being detected or null if stitcher was not identified */
    AdStitcherType getStitcher();

    /** Returns true if live ad stitching should be used*/
    boolean isLive();

    /** Returns the stream type being detected.*/
    @Nullable EnigmaMediaFormat.StreamFormat getStreamFormat();
}
