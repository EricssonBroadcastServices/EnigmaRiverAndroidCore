// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;

import org.json.JSONObject;

/**
 * Represents the ads response from exposure. These models are subjects for changes
 */
public class ExposureAdMetadata implements IAdMetadata {

    private AdStitcherType stitcher;
    @Nullable public final String stitcherSession;
    @Nullable public final  String stitcherProfileId;
    private final boolean isLive;
    private long insertionDuration;
    private long insertionMaxCount;
    private EnigmaMediaFormat.StreamFormat streamFormat;

    public ExposureAdMetadata(@Nullable JSONObject jsonObject, EnigmaMediaFormat.StreamFormat streamFormat, boolean isLive) {
        this.isLive = isLive;
        if(jsonObject == null) {
            stitcherSession = stitcherProfileId = null;
            return;
        }

        stitcher = parseStitcher(jsonObject.optString("stitcher"));
        stitcherSession = jsonObject.optString("stitcherSession");
        stitcherProfileId = jsonObject.optString("stitcherProfileId");
        insertionDuration = jsonObject.optLong("insertionDuration");
        insertionMaxCount = jsonObject.optLong("insertionMaxCount");

        this.streamFormat = streamFormat;
    }

    private AdStitcherType parseStitcher(String name) {
        if(name == null) { return AdStitcherType.Unknown; }
        if(name.toLowerCase().equals("nowtilus")) {
            return AdStitcherType.Nowtilus;
        } else if(name.toLowerCase().equals("generic")) {
            return AdStitcherType.None;
        }
        return AdStitcherType.Unknown;
    }

    public AdStitcherType getStitcher() { return stitcher; }

    @Override
    public boolean isLive() { return isLive; }

    @Nullable public EnigmaMediaFormat.StreamFormat getStreamFormat() { return streamFormat; }

}
