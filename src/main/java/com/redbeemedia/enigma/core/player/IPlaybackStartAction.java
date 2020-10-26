package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.EnigmaError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ interface IPlaybackStartAction {
    void start();
    void cancel();

    void onStarted(IInternalPlaybackSession internalPlaybackSession);

    void onErrorDuringStartup(EnigmaError error);

    interface IEnigmaPlayerCallbacks {
        void setStateIfCurrentStartAction(IPlaybackStartAction action, EnigmaPlayerState state);
        void deliverPlaybackSession(IInternalPlaybackSession internalPlaybackSession);
        IPlaybackSessionInfo getPlaybackSessionInfo(String manifestUrl);

        JSONObject getUsableMediaFormat(JSONArray formats) throws JSONException;
    }
}
