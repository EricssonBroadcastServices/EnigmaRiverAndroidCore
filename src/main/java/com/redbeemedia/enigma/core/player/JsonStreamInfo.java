package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.time.Duration;

import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ class JsonStreamInfo implements IStreamInfo {
    private boolean live;
    private boolean staticManifest;
    private boolean event;
    private Duration startSinceEpoch = null;
    private Duration endSinceEpoch = null;
    private String channelId;
    private String programId;

    public JsonStreamInfo(JSONObject streamInfo) throws JSONException {
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

    @Override
    public boolean isLiveStream() {
        return live && !staticManifest;
    }

    @Override
    public boolean isEvent() {
        return event;
    }

    @Override
    public boolean hasStart() {
        return startSinceEpoch != null;
    }

    @Override
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

    @Override
    public boolean hasChannelId() {
        return channelId != null;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    public String getProgramId() {
        return programId;
    }

    public boolean hasStaticManifest() {
        return staticManifest;
    }

    public static JsonStreamInfo createForNull() {
        try {
            return new JsonStreamInfo(null);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
