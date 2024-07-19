// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;

public class BasePlayResultHandler implements IPlayResultHandler {
    @Deprecated
    @Override
    public final void _dont_implement_IPlayResultHandler___instead_extend_BasePlayResultHandler_() {
    }

    @Override
    public void onStarted(IPlaybackSession playbackSession) {
    }

    @Override
    public void onError(EnigmaError error) {
    }
}
