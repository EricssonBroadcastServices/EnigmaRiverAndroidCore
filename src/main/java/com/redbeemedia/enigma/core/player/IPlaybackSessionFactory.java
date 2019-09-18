package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.drm.IDrmInfo;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.session.ISession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ interface IPlaybackSessionFactory {

    void startAsset(ISession session, IPlaybackProperties playbackProperties, IPlayResultHandler playResultHandler, String assetId, IEnigmaPlayerCallbacks playerConnector);

    interface IEnigmaPlayerCallbacks {
        void deliverPlaybackSession(IInternalPlaybackSession internalPlaybackSession);
        JSONObject getUsableMediaFormat(JSONArray formats) throws JSONException;
        void setDrmInfo(IDrmInfo drmInfo);
        IPlaybackSessionInfo getPlaybackSessionInfo(String manifestUrl);
        void loadIntoPlayerImplementation(String manifestUrl, IPlayResultHandler playResultHandler, JSONObject jsonObject, IPlaybackProperties playbackProperties);
        void setStateIfCurrentStartAction(EnigmaPlayerState state);
    }
}
