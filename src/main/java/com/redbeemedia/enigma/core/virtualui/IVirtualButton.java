// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.virtualui;

import android.os.Handler;

/**
 * Do not extend or implement this interface directly. Instead extend {@link AbstractVirtualButton}
 */
public interface IVirtualButton {
    /**
     * <p>A virtual button is considered <b>enabled</b> if it can currently be clicked.</p>
     * <br>
     * <p>Furthermore, isEnabled() implies isRelevant(). </p>
     * @return if the button is currently enabled
     */
    boolean isEnabled();

    /**
     * <p>A virtual button is considered <b>relevant</b> if it is relevant for the current stream.
     * For example, {@link com.redbeemedia.enigma.core.virtualui.impl.GoToLiveButton GoToLiveButton}
     * is relevant for a live stream, but only enabled when not already playing from the live edge.</p>
     * <br>
     * <p>Furthermore, isEnabled() implies isRelevant(). </p>
     * @return if the button is currently enabled
     */
    boolean isRelevant();

    void click();

    void refresh();

    boolean addListener(IVirtualButtonListener listener);
    boolean addListener(IVirtualButtonListener listener, Handler handler);
    boolean removeListener(IVirtualButtonListener listener);
}
