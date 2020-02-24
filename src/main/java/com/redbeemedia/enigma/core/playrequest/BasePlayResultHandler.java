package com.redbeemedia.enigma.core.playrequest;

import androidx.annotation.NonNull;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;

public class BasePlayResultHandler implements IPlayResultHandler {
    @Deprecated
    @Override
    public final void _dont_implement_IPlayResultHandler___instead_extend_BasePlayResultHandler_() {
    }

    @Override
    public void onStarted(@NonNull IPlaybackSession playbackSession) {
    }

    @Override
    public void onError(@NonNull EnigmaError error) {
    }
}
