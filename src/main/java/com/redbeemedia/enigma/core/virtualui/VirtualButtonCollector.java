// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.virtualui;

import com.redbeemedia.enigma.core.util.Collector;

/*package-protected*/ class VirtualButtonCollector extends Collector<IVirtualButtonListener> implements IVirtualButtonListener {
    public VirtualButtonCollector() {
        super(IVirtualButtonListener.class);
    }

    @Deprecated
    @Override
    public final void _dont_implement_IVirtualButtonListener___instead_extend_BaseVirtualButtonListener_() {
    }

    @Override
    public void onStateChanged() {
        forEach(listener -> listener.onStateChanged());
    }
}
