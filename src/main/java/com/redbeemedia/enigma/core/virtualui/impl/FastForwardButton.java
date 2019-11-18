package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;

/*package-protected*/ class FastForwardButton extends AbstractSeekButton {

    public FastForwardButton(IVirtualButtonContainer container) {
        super(container);
    }


    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        if(!super.calculateRelevant(container)) {
            return false;
        }
        IContractRestrictions contractRestrictions = container.getContractRestrictions();
        return contractRestrictions == null || contractRestrictions.getValue(ContractRestriction.FASTFORWARD_ENABLED, true);
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
        ITimelinePosition currentPosition = container.getEnigmaPlayer().getTimeline().getCurrentPosition();
        if(currentPosition != null) {
            ITimelinePosition seekPosition = currentPosition.add(container.getSettings().getSeekForwardStep());
            container.getPlayerControls().seekTo(seekPosition);
        }
    }
}
