package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.session.ISession;

import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ interface IPlaybackSessionFactory {
    IInternalPlaybackSession createPlaybackSession(PlaybackSessionArgs parameters) throws JSONException;

    /*package-protected*/ class PlaybackSessionArgs {
        public ISession session;
        public JSONObject jsonObject;
        public IPlaybackSessionInfo playbackSessionInfo;

        public PlaybackSessionArgs(ISession session, JSONObject jsonObject, IPlaybackSessionInfo playbackSessionInfo) {
            this.session = session;
            this.jsonObject = jsonObject;
            this.playbackSessionInfo = playbackSessionInfo;
        }
    }
}
