package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.error.Error;

public interface IPlayResultHandler {
    void onStarted(Object object);
    void onError(Error error);
}
