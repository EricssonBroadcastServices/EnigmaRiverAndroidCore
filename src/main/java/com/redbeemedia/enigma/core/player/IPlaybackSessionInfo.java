// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.time.Duration;

/*package-protected*/ interface IPlaybackSessionInfo {
    Duration getCurrentPlaybackOffset();
    String getAssetId();
    String getPlaybackSessionId();
    IPlayable getPlayable();
    String getMediaLocator();
    String getPlayerTechnologyName();
    String getPlayerTechnologyVersion();
    String getCurrentProgramId();
    IPlaybackProperties getPlaybackProperties();
    String getCdnProvider();
    Integer getDuration();
}
