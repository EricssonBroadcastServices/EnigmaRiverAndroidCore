package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;

public class MockPlayResultHandler extends BasePlayResultHandler {
    @Override
    public void onStarted(IPlaybackSession playbackSession) {
    }

    @Override
    public void onError(Error error) {
    }
}
