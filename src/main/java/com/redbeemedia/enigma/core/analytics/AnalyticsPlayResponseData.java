package com.redbeemedia.enigma.core.analytics;

import org.json.JSONObject;

/** Contains analytics information from the play response. */
public class AnalyticsPlayResponseData {

    private static int FALLBACK_POST_FREQUENCY_S = 60;

    public final String profile;
    public final String host;
    public final String provider;
    public final String tag;
    public final int bucket;
    public final int postIntervalSeconds;
    public final String streamingTechnology;

    /** Will be true if sufficient data was provided. */
    public final boolean initialized;

    public AnalyticsPlayResponseData(JSONObject playResponse, String streamingTechnology) {
        JSONObject analytics = playResponse.optJSONObject("analytics");
        JSONObject cdn = playResponse.optJSONObject("cdn");

        initialized = (cdn != null && analytics != null);

        profile = initialized ? cdn.optString("profile", null) : null;
        host = initialized ? cdn.optString("host", null) : null;
        provider = initialized ? cdn.optString("provider", null) : null;
        tag = initialized ? analytics.optString("tag", null) : null;
        bucket = initialized ? analytics.optInt("bucket", 0) : 0;
        postIntervalSeconds = initialized ? analytics.optInt("postInterval", FALLBACK_POST_FREQUENCY_S) : FALLBACK_POST_FREQUENCY_S;

        this.streamingTechnology = streamingTechnology;
    }

}
