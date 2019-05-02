package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IPlayerImplementationControlResultHandler {
    void onRejected(IControlResultHandler.IRejectReason rejectReason);
    void onCancelled();
    void onError(Error error);
    void onDone();
}
