package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.ads.IAdDetector;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.marker.IMarkerPointsDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ interface IPlaybackStartAction {
    void start();
    void cancel();

    void onStarted(IInternalPlaybackSession internalPlaybackSession);

    void onErrorDuringStartup(EnigmaError error);

    void setAdDetector(IAdDetector adDetector);
    void setMarkerPointsDetector(IMarkerPointsDetector markerPointsDetector);

    interface IEnigmaPlayerCallbacks {
        void setStateIfCurrentStartAction(IPlaybackStartAction action, EnigmaPlayerState state);
        void deliverPlaybackSession(IInternalPlaybackSession internalPlaybackSession);
        IPlaybackSessionInfo getPlaybackSessionInfo(String assetId, String manifestUrl, String cdnProvider, String playbackSessionId, Integer duration);

        JSONObject getUsableMediaFormat(JSONArray formats) throws JSONException;
    }
}
