// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

import java.util.List;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public interface IStateMachineBuilder<S> {
    void addState(S state);
    void setInitialState(S state);
    void addDirectTransition(S fromState, S toState);
    IStateMachine<S> build();
    void setInvalidStateTransitionHandler(IInvalidStateTransitionHandler<S> handler);

    interface IInvalidStateTransitionHandler<S> {
        /**
         * @param fromState
         * @param toState
         * @return an alternative path from fromState (exclusive) to toState (inclusive)
         */
        List<S> onInvalidStateTransition(S fromState, S toState);
    }
}
