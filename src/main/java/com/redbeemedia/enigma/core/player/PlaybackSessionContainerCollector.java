package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.util.Collector;

/*package-protected*/ class PlaybackSessionContainerCollector extends Collector<IPlaybackSessionContainerListener> implements IPlaybackSessionContainerListener {
    public PlaybackSessionContainerCollector() {
        super(IPlaybackSessionContainerListener.class);
    }

    @Override
    public void onPlaybackSessionChanged(IInternalPlaybackSession oldSession, IInternalPlaybackSession newSession) {
        forEach(listener -> listener.onPlaybackSessionChanged(oldSession, newSession));
    }
}
