package com.redbeemedia.enigma.core.player;

import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ class StreamInfo {
    private boolean liveStream;
    private boolean event;
    private long startUtcSeconds = -1L;

    public StreamInfo(JSONObject streamInfo) throws JSONException {
		/*"streamInfo" : {
			"live" : ${bool},
            "static" : ${bool},
            "event" : ${bool},
            "start" : ${UTC of start in SECONDS!},
            "channelId" : "...",
            "programId" : "..."
        }*/
        if(streamInfo != null) {
            this.liveStream = streamInfo.optBoolean("live", false);
            if(liveStream) {
                this.startUtcSeconds = streamInfo.getLong("start");
            }
            this.event = streamInfo.optBoolean("event", false);
        }
    }

    public boolean isLiveStream() {
        return liveStream;
    }

    public long getStartUtcSeconds() {
        return startUtcSeconds;
    }

    public static StreamInfo createForNull() {
        try {
            return new StreamInfo(null);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
