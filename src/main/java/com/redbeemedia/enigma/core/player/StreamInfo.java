package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.time.Duration;

import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ class StreamInfo {
    private boolean live;
    private boolean staticManifest;
    private boolean event;
    private Duration startSinceEpoch = null;
    private Duration endSinceEpoch = null;
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
            if(streamInfo.has("start")) {
                long startUtcSeconds = streamInfo.getLong("start");
                startSinceEpoch = Duration.seconds(startUtcSeconds);
            }
            if(streamInfo.has("end")) {
                long endUtcSeconds = streamInfo.getLong("end");
                endSinceEpoch = Duration.seconds(endUtcSeconds);
            }
            this.event = streamInfo.optBoolean("event", false);
            this.channelId = streamInfo.optString("channelId", null);
            this.programId = streamInfo.optString("programId", null);
        }
    }

    public boolean isLiveStream() {
        return live && !staticManifest;
    }

    public boolean hasStart() {
        return startSinceEpoch != null;
    }

    public long getStart(Duration.Unit units) {
        if(startSinceEpoch == null) {
            throw new IllegalArgumentException("startSinceEpoch never set");
        }
        return startSinceEpoch.inWholeUnits(units);
    }

    public boolean hasEnd() {
        return endSinceEpoch != null;
    }

    public long getEnd(Duration.Unit units) {
        if(endSinceEpoch == null) {
            throw new IllegalArgumentException("endSinceEpoch never set");
        }
        return endSinceEpoch.inWholeUnits(units);
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
        return hasStart() && channelId != null;
    }
}
