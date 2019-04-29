package com.redbeemedia.enigma.core.util;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public interface IStateMachineBuilder<S> {
    void addState(S state);
    void setInitialState(S state);
    void addDirectTransition(S fromState, S toState);
    IStateMachine<S> build();
}
