package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.playable.IPlayable;

public interface IPlayRequest {
    @Deprecated
    void onStarted(); //TODO remove
    @Deprecated
    void onError(Error error);//TODO remove

    IPlayable getPlayable();
    IPlayResultHandler getResultHandler();
}
