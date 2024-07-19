// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

/** The type of available ad events corresponding to a relative position in an ad.*/
public enum AdEventType {
    Start,
    Loaded,
    FirstQuartile,
    MidPoint,
    ThirdQuartile,
    Complete
};
