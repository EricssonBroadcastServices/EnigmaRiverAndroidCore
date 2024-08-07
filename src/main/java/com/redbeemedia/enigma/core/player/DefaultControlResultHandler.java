// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import android.util.Log;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;

/*package-protected*/ class DefaultControlResultHandler implements IControlResultHandler {
    private final String tag;

    public DefaultControlResultHandler(String tag) {
        this.tag = tag;
    }

    @Override
    public void onRejected(IRejectReason reason) {
        Log.d(tag, "ControlRequest was rejected. ("+(reason != null ? reason.getType(): "null reason")+")");
    }

    @Override
    public void onCancelled() {
        Log.i(tag, "ControlRequest cancelled.");
    }

    @Override
    public void onError(EnigmaError error) {
        Log.e(tag, "ControlRequest got error "+error.getClass().getSimpleName()+" ("+error.getErrorCode()+")");
    }

    @Override
    public void onDone() {
    }
}
