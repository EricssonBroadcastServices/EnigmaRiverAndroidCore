package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;

/*package-protected*/ class SeekBar extends AbstractVirtualButtonImpl {

    public SeekBar(IVirtualButtonContainer container) {
        super(container);
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        return true;
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        if(container.getEnigmaPlayer().isAdBeingPlayed()){
            return false;
        }
        return true;
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
