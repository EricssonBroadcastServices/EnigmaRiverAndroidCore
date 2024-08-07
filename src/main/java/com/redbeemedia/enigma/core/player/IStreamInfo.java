// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.time.Duration;

/*package-protected*/ interface IStreamInfo {
    boolean isLiveStream();
    boolean isEvent();

    boolean hasStart();
    long getStart(Duration.Unit unit);

    boolean hasChannelId();
    String getChannelId();

    String getPlayMode();

    /** Returns `true` if server side ad insertion (SSAI) is enabled. */
    boolean ssaiEnabled();

    Duration getLiveDelay();

    /** returns if MediaType is audio or video **/
    MediaType getMediaType();
}
