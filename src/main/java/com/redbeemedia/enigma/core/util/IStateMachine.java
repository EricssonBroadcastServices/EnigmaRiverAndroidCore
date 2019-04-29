package com.redbeemedia.enigma.core.util;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public interface IStateMachine<S> {
    boolean addListener(IStateChangedListener<S> listener);
    boolean removeListener(IStateChangedListener<S> listener);
    S getState();
    void setState(S newState);
}
