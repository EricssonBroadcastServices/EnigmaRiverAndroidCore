package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ interface IPlaybackSessionFactory {
    IInternalPlaybackSession createPlaybackSession(ISession session, JSONObject jsonObject, ITimeProvider timeProvider) throws JSONException;
}
