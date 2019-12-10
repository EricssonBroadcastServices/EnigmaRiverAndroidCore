package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.player.ControlLogic;
import com.redbeemedia.enigma.core.player.EnigmaPlayerState;
import com.redbeemedia.enigma.core.player.InternalVirtualControlsUtil;

/*package-protected*/ class PlayButton extends AbstractVirtualButtonImpl {

    public PlayButton(IVirtualButtonContainer container) {
        super(container);
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateStart(container.getPlayerState(), container.getPlaybackSession(), InternalVirtualControlsUtil.hasPlaybackSessionSeed(container.getEnigmaPlayer()));
        return validationResults.isSuccess() && container.getPlayerState() != EnigmaPlayerState.PLAYING;
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
