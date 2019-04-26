package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/*package-protected*/ class DefaultPlaybackSessionFactory implements IPlaybackSessionFactory {
    @Override
    public InternalPlaybackSession createPlaybackSession(ISession session, JSONObject jsonObject, ITimeProvider timeProvider) throws JSONException {
        String playbackSessionId = jsonObject.optString("playSessionId", UUID.randomUUID().toString());
        StreamInfo streamInfo = new StreamInfo(jsonObject.optJSONObject("streamInfo"));
        return new InternalPlaybackSession(session, playbackSessionId, timeProvider, streamInfo);
    }
}
