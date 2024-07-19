// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IPlayerImplementationControlResultHandler {
    void onRejected(IControlResultHandler.IRejectReason rejectReason);
    void onCancelled();
    void onError(EnigmaError error);
    void onDone();
}
