package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;

/*package-protected*/ class BasePlayerImplementationControlResultHandler implements IPlayerImplementationControlResultHandler {
    @Override
    public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
    }

    @Override
    public void onCancelled() {
    }

    @Override
    public void onError(Error error) {
    }

    @Override
    public void onDone() {
    }
}
