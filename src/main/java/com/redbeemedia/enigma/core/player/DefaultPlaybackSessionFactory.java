package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONException;

/*package-protected*/ class DefaultPlaybackSessionFactory implements IPlaybackSessionFactory {
    private final ITimeProvider timeProvider;

    public DefaultPlaybackSessionFactory(ITimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public IInternalPlaybackSession createPlaybackSession(PlaybackSessionArgs parameters) throws JSONException {
        return new InternalPlaybackSession(InternalPlaybackSession.ConstructorArgs.of(parameters, timeProvider));
    }
}
