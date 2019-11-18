package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.player.EnigmaPlayerState;

/*package-protected*/ class PlayButton extends AbstractVirtualButtonImpl {

    public PlayButton(IVirtualButtonContainer container) {
        super(container);
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        return container.getPlayerState() != EnigmaPlayerState.PLAYING;
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        return true;
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
        container.getPlayerControls().start();
    }
}
