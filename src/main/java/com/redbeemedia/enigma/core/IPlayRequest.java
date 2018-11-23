package com.redbeemedia.enigma.core;

import com.redbeemedia.enigma.core.error.Error;

public interface IPlayRequest {
    void onStarted(); //TODO maybe add some started event as parameter
    void onError(Error error); //TODO playback-error?
    IPlayable getPlayable();
}
