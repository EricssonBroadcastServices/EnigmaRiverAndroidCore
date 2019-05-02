package com.redbeemedia.enigma.core.util;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public interface IStateChangedListener<S> extends IInternalListener {
    void onStateChanged(S from, S to);
}
