package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.playable.IPlayable;

public interface IPlayRequest {
    IPlayable getPlayable();
    IPlaybackProperties getPlaybackProperties();
    IPlayResultHandler getResultHandler();
}
