package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;

/*package-protected*/ class RestartButton extends AbstractVirtualButtonImpl {
    public RestartButton(IVirtualButtonContainer container) {
        super(container);
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        return !container.getEnigmaPlayer().isAdBeingPlayed();
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        IContractRestrictions contractRestrictions = container.getContractRestrictions();
        if(contractRestrictions != null) {
            return contractRestrictions.getValue(ContractRestriction.TIMESHIFT_ENABLED, true);
        } else {
            return true;
        }
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
        ITimelinePosition startBound = container.getEnigmaPlayer().getTimeline().getCurrentStartBound();
        if(startBound != null) {
            container.getPlayerControls().seekTo(startBound);
        } else {
            container.getPlayerControls().seekTo(IEnigmaPlayerControls.StreamPosition.START);
        }
    }
}
