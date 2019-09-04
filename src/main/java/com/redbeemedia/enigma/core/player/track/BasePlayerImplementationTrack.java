package com.redbeemedia.enigma.core.player.track;

import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;

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
}
