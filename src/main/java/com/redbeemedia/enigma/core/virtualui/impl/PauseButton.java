package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.player.ControlLogic;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;

/*package-protected*/ class PauseButton extends AbstractVirtualButtonImpl {
    public PauseButton(IVirtualButtonContainer container) {
        super(container);
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
        container.getPlayerControls().pause();
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validatePause(container.getPlayerState(), container.getPlaybackSession());
        return validationResults.isSuccess() && aimsToBePlayingAlready(container.getPlayerState());
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        IContractRestrictions contractRestrictions = container.getContractRestrictions();
        if(contractRestrictions != null) {
            return contractRestrictions.getValue(ContractRestriction.TIMESHIFT_ENABLED, true);
        }
        return true;
    }
}
