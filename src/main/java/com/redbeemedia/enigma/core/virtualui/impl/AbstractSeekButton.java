package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.ControlLogic;

/*package-protected*/ abstract class AbstractSeekButton extends AbstractVirtualButtonImpl {
    private final boolean forward;

    public AbstractSeekButton(IVirtualButtonContainer container, boolean forward) {
        super(container);
        this.forward = forward;
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateSeek(forward, !forward, container.getPlaybackSession());
        return validationResults.isSuccess();
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        IPlaybackSession playbackSession = container.getPlaybackSession();
        if(playbackSession != null) {
            if(!playbackSession.isSeekAllowed()) {
                return false;
            }
        }
        return true;
    }
}
