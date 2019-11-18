package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;

/*package-protected*/ class RewindButton extends AbstractSeekButton {

    public RewindButton(IVirtualButtonContainer container) {
        super(container);
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        if(!super.calculateRelevant(container)) {
            return false;
        }
        IContractRestrictions contractRestrictions = container.getContractRestrictions();
        return contractRestrictions == null || contractRestrictions.getValue(ContractRestriction.REWIND_ENABLED, true);
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
        ITimelinePosition currentPosition = container.getEnigmaPlayer().getTimeline().getCurrentPosition();
        if(currentPosition != null) {
            ITimelinePosition seekPosition = currentPosition.subtract(container.getSettings().getSeekBackwardStep());
            container.getPlayerControls().seekTo(seekPosition);
        }
    }
}
