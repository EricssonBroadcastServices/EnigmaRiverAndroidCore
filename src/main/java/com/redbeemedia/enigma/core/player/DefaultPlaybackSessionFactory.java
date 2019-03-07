package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONObject;

import java.util.UUID;

/*package-protected*/ class DefaultPlaybackSessionFactory implements IPlaybackSessionFactory {
    @Override
    public PlaybackSession createPlaybackSession(ISession session, JSONObject jsonObject, ITimeProvider timeProvider) {
        String playbackSessionId = jsonObject.optString("playSessionId", UUID.randomUUID().toString());
        return new PlaybackSession(session, playbackSessionId, timeProvider);
    }
}