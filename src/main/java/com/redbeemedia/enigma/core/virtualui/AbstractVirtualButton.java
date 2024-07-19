// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.virtualui;

import android.os.Handler;

import com.redbeemedia.enigma.core.util.HandlerWrapper;

public abstract class AbstractVirtualButton implements IVirtualButton {
    private final VirtualButtonCollector collector = new VirtualButtonCollector();

    @Override
    public boolean addListener(IVirtualButtonListener listener) {
        return collector.addListener(listener);
    }

    @Override
    public boolean addListener(IVirtualButtonListener listener, Handler handler) {
        return collector.addListener(listener, new HandlerWrapper(handler));
    }

    @Override
    public boolean removeListener(IVirtualButtonListener listener) {
        return collector.removeListener(listener);
    }

    protected final IVirtualButtonListener fireEvent() {
        return collector;
    }
}
