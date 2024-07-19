// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.session.ISession;

/**
 * Utilize the 'Offline play request' when you intend to request playback for downloaded/offline content
 */
public class OfflinePlayRequest extends PlayRequest {

    public OfflinePlayRequest(IPlayable playable, IPlayResultHandler resultHandler) {
        this(playable, new OfflinePlaybackProperties(), resultHandler);
    }

    public OfflinePlayRequest(ISession session, IPlayable playable, IPlayResultHandler resultHandler) {
        this(session, playable, new OfflinePlaybackProperties(), resultHandler);
    }

    public OfflinePlayRequest(IPlayable playable, OfflinePlaybackProperties playbackProperties, IPlayResultHandler resultHandler) {
        this(null, playable, playbackProperties, resultHandler);
    }

    public OfflinePlayRequest(ISession session, IPlayable playable, OfflinePlaybackProperties playbackProperties, IPlayResultHandler resultHandler) {
        super(session, playable, playbackProperties, resultHandler);
    }
}
