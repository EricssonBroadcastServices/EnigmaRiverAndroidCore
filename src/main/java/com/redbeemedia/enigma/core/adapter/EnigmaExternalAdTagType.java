/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.adapter;

import java.util.Locale;

public enum EnigmaExternalAdTagType {
    VAST,
    VMAP,
    IMA,
    PROGRESSIVE,
    BITMOVIN,
    UNKNOWN;

    public static EnigmaExternalAdTagType fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "vast":
                return VAST;
            case "vmap":
                return VMAP;
            case "ima":
                return IMA;
            case "progressive":
                return PROGRESSIVE;
            case "bitmovin":
                return BITMOVIN;
            default:
                return UNKNOWN;
        }
    }
}