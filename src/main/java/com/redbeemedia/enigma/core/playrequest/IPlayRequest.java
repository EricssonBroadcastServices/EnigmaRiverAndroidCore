package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.playable.IPlayable;

public interface IPlayRequest {
    void onStarted(); //TODO maybe add some started event as parameter
    void onError(Error error); //TODO playback-error?
    IPlayable getPlayable();
}
