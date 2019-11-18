package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

/*package-protected*/ class GoToLiveButton extends AbstractVirtualButtonImpl {
    private final OpenContainer<Boolean> atLivePoint = new OpenContainer<>(false);


    public GoToLiveButton(IVirtualButtonContainer container) {
        super(container);
        container.getEnigmaPlayer().addListener(new BaseEnigmaPlayerListener() {
            private final IPlaybackSessionListener playbackSessionListener = new BasePlaybackSessionListener() {
                @Override
                public void onPlayingFromLiveChanged(boolean live) {
                    setPlayingFromLive(live);
                }
            };

            @Override
            public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
                if(from != null) {
                    from.removeListener(playbackSessionListener);
                }
                if(to != null) {
                    setPlayingFromLive(to.isPlayingFromLive());
                    to.addListener(playbackSessionListener);
                }
            }
        });
    }

    private void setPlayingFromLive(boolean live) {
        OpenContainerUtil.setValueSynchronized(atLivePoint, live, (oldValue, newValue) -> refresh());
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        return OpenContainerUtil.getValueSynchronized(atLivePoint);
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        IPlaybackSession playbackSession = container.getPlaybackSession();
        if(playbackSession != null) {
            return playbackSession.isSeekToLiveAllowed();
        } else {
            return false;
        }
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
        container.getPlayerControls().seekTo(IEnigmaPlayerControls.StreamPosition.LIVE_EDGE);
    }
}
