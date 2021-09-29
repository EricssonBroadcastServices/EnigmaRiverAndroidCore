package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.ControlLogic;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.time.Duration;

/*package-protected*/ abstract class AbstractSeekButton extends AbstractVirtualButtonImpl {
    private final boolean forward;

    public AbstractSeekButton(IVirtualButtonContainer container, boolean forward) {
        super(container);
        this.forward = forward;
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        if(container.getEnigmaPlayer().isAdBeingPlayed()){
            return false;
        }
        if(forward) {
            ITimeline timeline = container.getEnigmaPlayer().getTimeline();
            ITimelinePosition currentPosition = timeline.getCurrentPosition();
            ITimelinePosition livePosition = timeline.getLivePosition();
            Duration vicinity = container.getSettings().getLivePositionVicinityThreshold();
            if(livePosition != null) {
                Duration diff = livePosition.subtract(currentPosition);
                if(diff.compareTo(vicinity) <= 0) {
                    return false;
                }
            }
        }
        ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateSeek(forward, !forward, container.getPlaybackSession());
        if (container.getPlaybackSession() != null
                && !container.getPlaybackSession().isSeekInLiveAllowed()
                && container.getEnigmaPlayer().isLiveStream()) {
            return false;
        }
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
