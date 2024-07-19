// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.player.EnigmaPlayerState;
import com.redbeemedia.enigma.core.util.AndroidThreadUtil;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;
import com.redbeemedia.enigma.core.virtualui.AbstractVirtualButton;

/*package-protected*/ abstract class AbstractVirtualButtonImpl extends AbstractVirtualButton {
    private final IVirtualButtonContainer container;
    private final OpenContainer<Boolean> enabled;
    private final OpenContainer<Boolean> relevant = new OpenContainer<>(true);

    public AbstractVirtualButtonImpl(IVirtualButtonContainer container) {
        this(container, true);
    }

    public AbstractVirtualButtonImpl(IVirtualButtonContainer container, boolean defaultEnabled) {
        container.addButton(this);
        this.container = container;
        enabled = new OpenContainer<>(defaultEnabled);
    }

    @Override
    public final boolean isEnabled() {
        return OpenContainerUtil.getValueSynchronized(enabled);
    }

    @Override
    public final boolean isRelevant() {
        return OpenContainerUtil.getValueSynchronized(relevant);
    }

    protected final void setEnabled(boolean newEnabled) {
        StateChangedFlag stateChanged = new StateChangedFlag();
        OpenContainerUtil.setValueSynchronized(enabled, newEnabled, (oldValue, newValue) -> stateChanged.registerChange());
        if(stateChanged.hasChanged()) {
            fireEvent().onStateChanged();
        }
    }

    protected static boolean aimsToBePlayingAlready(EnigmaPlayerState state) {
        return state == EnigmaPlayerState.PLAYING || state == EnigmaPlayerState.BUFFERING;
    }

    @Override
    public final void click() {
        AndroidThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refresh();
                if(isEnabled()) {
                    onClick(container);
                }
            }
        });
    }

    @Override
    public void refresh() {
        StateChangedFlag stateChanged = new StateChangedFlag();

        boolean newRelevant;
        try {
            newRelevant = calculateRelevant(container);
        } catch (Exception e) {
            e.printStackTrace(); //Log
            newRelevant = false;
        }
        OpenContainerUtil.setValueSynchronized(relevant, newRelevant, (oldValue, newValue) -> stateChanged.registerChange());

        boolean newEnabled;
        try {
            newEnabled = newRelevant && calculateEnabled(container);
        } catch (Exception e) {
            e.printStackTrace(); //Log
            newEnabled = false;
        }
        OpenContainerUtil.setValueSynchronized(enabled, newEnabled, (oldValue, newValue) -> stateChanged.registerChange());

        if(stateChanged.hasChanged()) {
            fireEvent().onStateChanged();
        }
    }

    protected abstract boolean calculateRelevant(IVirtualButtonContainer container) throws Exception;

    protected abstract boolean calculateEnabled(IVirtualButtonContainer container) throws Exception;

    protected abstract void onClick(IVirtualButtonContainer container);

    private static class StateChangedFlag {
        private boolean hasChanged = false;
        public void registerChange() {
            hasChanged = true;
        }
        public boolean hasChanged() {
            return hasChanged;
        }

    }
}
