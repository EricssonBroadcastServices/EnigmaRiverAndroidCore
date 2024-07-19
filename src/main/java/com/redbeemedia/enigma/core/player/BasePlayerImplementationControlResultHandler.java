// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;

/*package-protected*/ class BasePlayerImplementationControlResultHandler implements IPlayerImplementationControlResultHandler {
    @Override
    public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
    }

    @Override
    public void onCancelled() {
    }

    @Override
    public void onError(EnigmaError error) {
    }

    @Override
    public void onDone() {
    }
}
