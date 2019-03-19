package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.Error;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IPlayerImplementationListener {
    void onError(Error error);
}
