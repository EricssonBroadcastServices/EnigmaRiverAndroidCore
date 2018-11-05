package com.redbeemedia.enigma.core;

public interface IPlayRequest {
    void onStarted(); //TODO maybe add some started event as parameter
    void onError(String errorMessage); //TODO use an error event instead
    IPlayable getPlayable();
}
