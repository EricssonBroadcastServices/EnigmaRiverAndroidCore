/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.adapter;

public class EnigmaExternalAdMarker {
    private final String type;
    private final String tagType;
    private final EnigmaExternalAdTagType resolvedTagType;
    private final String adTag;

    public EnigmaExternalAdMarker(String type, String tagType, String adTag) {
        this.type = type;
        this.tagType = tagType;
        this.resolvedTagType = EnigmaExternalAdTagType.fromString(tagType);
        this.adTag = adTag;
    }

    public String getType() { return type; }

    public String getTagType() { return tagType; }

    public EnigmaExternalAdTagType getResolvedTagType() { return resolvedTagType; }

    public String getAdTag() { return adTag; }
}