package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;

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
        if (container.getEnigmaPlayer().isAdBeingPlayed() || container.getPlaybackSession() == null) {
            return false;
        } else if (container.getPlaybackSession() != null
                && !container.getPlaybackSession().isSeekInLiveAllowed()
                && container.getEnigmaPlayer().isLiveStream()) {
            // if live stream and seek live is not allowed
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
