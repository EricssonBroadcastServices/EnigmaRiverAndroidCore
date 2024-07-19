// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

import android.os.Handler;

public class HandlerWrapper implements IHandler {
    private Handler handler;

    public HandlerWrapper(Handler handler) {
        if(handler == null) {
            throw new IllegalArgumentException("handler is null");
        }
        this.handler = handler;
    }

    @Override
    public boolean post(Runnable runnable) {
        return handler.post(runnable);
    }

    @Override
    public boolean postDelayed(Runnable runnable, long delayMillis) {
        return handler.postDelayed(runnable, delayMillis);
    }
}
