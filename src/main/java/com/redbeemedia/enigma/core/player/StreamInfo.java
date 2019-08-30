package com.redbeemedia.enigma.core.player;

import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ class StreamInfo {
    private boolean live;
    private boolean staticManifest;
    private boolean event;
    private long startUtcSeconds = -1L;
    private long endUtcSeconds = -1L;
    private String channelId;
    private String programId;

    public StreamInfo(JSONObject streamInfo) throws JSONException {
		/*"streamInfo" : {
			"live" : ${bool},
            "static" : ${bool},
            "event" : ${bool},
            "start" : ${UTC of start in SECONDS!},
            (optional) "end" : ${UTC of end in SECONDS!}, (sometimes present (for example, for catch-up))
            "channelId" : "...",
            "programId" : "..."
        }*/
        if(streamInfo != null) {
            this.live = streamInfo.optBoolean("live", false);
            this.staticManifest = streamInfo.optBoolean("static", true);
            this.startUtcSeconds = streamInfo.optLong("start", -1L);
            this.endUtcSeconds = streamInfo.optLong("end", -1L);
            this.event = streamInfo.optBoolean("event", false);
            this.channelId = streamInfo.optString("channelId");
            this.programId = streamInfo.optString("programId");
        }
    }

    public boolean isLiveStream() {
        return live && !staticManifest;
    }

    public boolean hasStartUtcSeconds() {
        return startUtcSeconds != -1L;
    }

    public long getStartUtcSeconds() {
        if(startUtcSeconds == -1L) {
            throw new IllegalArgumentException("startUtcSeconds never set");
        }
        return startUtcSeconds;
    }

    public boolean hasEndUtcSeconds() {
        return endUtcSeconds != -1L;
    }

    public long getEndUtcSeconds() {
        if(endUtcSeconds == -1L) {
            throw new IllegalArgumentException("endUtcSeconds never set");
        }
        return endUtcSeconds;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getProgramId() {
        return programId;
    }

    public boolean hasStaticManifest() {
        return staticManifest;
    }

    public static StreamInfo createForNull() {
        try {
            return new StreamInfo(null);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPlayMode() {
        if(hasStreamPrograms()) {
            return "CATCHUP";
        } else if(isLiveStream()) {
            return "LIVE";
        } else {
            return "VOD";
        }
    }

    public boolean hasStreamPrograms() {
        return hasStartUtcSeconds() && hasEndUtcSeconds() && channelId != null;
    }
}
