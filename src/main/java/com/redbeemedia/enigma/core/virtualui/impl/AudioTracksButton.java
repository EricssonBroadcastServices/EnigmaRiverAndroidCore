// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.virtualui.impl;

/*package-protected*/ class AudioTracksButton extends AbstractVirtualButtonImpl {
    public AudioTracksButton(IVirtualButtonContainer container) {
        super(container);
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) throws Exception {
        return true;
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) throws Exception {
        if (container.getPlaybackSession() != null) {
            return container.getPlaybackSession().getAudioTracks().size() > 1;
        }
        return false;
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
    }
}