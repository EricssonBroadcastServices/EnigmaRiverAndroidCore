package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.ControlLogic;
import com.redbeemedia.enigma.core.player.InternalVirtualControlsUtil;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;

/*package-protected*/ class JumpProgramButton extends AbstractVirtualButtonImpl {
    private final boolean jumpBackwards;

    public JumpProgramButton(IVirtualButtonContainer container, boolean jumpBackwards) {
        super(container);
        this.jumpBackwards = jumpBackwards;
        container.getEnigmaPlayer().addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onProgramChanged(IProgram from, IProgram to) {
                refresh();
            }
        });
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        if(container.getEnigmaPlayer().isAdBeingPlayed()){
            return false;
        }
        IPlaybackSession playbackSession = container.getPlaybackSession();
        ControlLogic.IValidationResults validationResults = ControlLogic.validateProgramJump(jumpBackwards, playbackSession);
        return validationResults.isSuccess();
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        return InternalVirtualControlsUtil.hasStreamPrograms(container.getPlaybackSession());
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
        if(jumpBackwards) {
            container.getPlayerControls().previousProgram();
        } else {
            container.getPlayerControls().nextProgram();
        }
    }
}
