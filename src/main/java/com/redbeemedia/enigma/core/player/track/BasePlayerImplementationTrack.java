// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player.track;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.video.IVideoTrack;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public class BasePlayerImplementationTrack implements IPlayerImplementationTrack {

    protected <T> T asType(Class<T> type) {
        if(type.isAssignableFrom(getClass())) {
            return (T) this;
        } else {
            return null;
        }
    }

    @Override
    public ISubtitleTrack asSubtitleTrack() {
        return asType(ISubtitleTrack.class);
    }

    @Override
    public IAudioTrack asAudioTrack() {
        return asType(IAudioTrack.class);
    }

    @Override
    public IVideoTrack asVideoTrack() {
        return asType(IVideoTrack.class);
    }
}
