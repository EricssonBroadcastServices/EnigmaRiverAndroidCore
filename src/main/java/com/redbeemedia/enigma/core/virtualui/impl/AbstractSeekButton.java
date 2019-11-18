package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;

/*package-protected*/ abstract class AbstractSeekButton extends AbstractVirtualButtonImpl {
    public AbstractSeekButton(IVirtualButtonContainer container) {
        super(container);
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        return true;
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
