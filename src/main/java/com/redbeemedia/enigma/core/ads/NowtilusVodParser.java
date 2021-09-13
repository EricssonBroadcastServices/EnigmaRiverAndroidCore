package com.redbeemedia.enigma.core.ads;

import android.util.Log;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/** Responsible for pasing VOD json data. */
class NowtilusVodParser implements INowtilusParser {

    private final static String TAG = NowtilusVodParser.class.getName();

    private final static String CONTENT_AD = "ad";
    private final static String CONTENT_VOD = "vod";
    private final static String CONTENT_DEFAULT = "content";

    @Nullable
    public VastAdEntrySet parseEntries(JSONObject resource) throws JSONException {
        ArrayList<VastAdEntry> entries = new ArrayList<VastAdEntry>();

        if(!resource.has("clips")) {
            Log.w(TAG, "Resource contains no clips");
            return new VastAdEntrySet(entries);
        }

        JSONArray clips = resource.getJSONArray("clips");
        long adStartTime = 0;
        for(int i = 0; i < clips.length(); i++) {
            JSONObject clip = clips.getJSONObject(i);
            String category = clip.optString("category");
            if(category != null) {
                if(category.equals(CONTENT_AD)) {
                    VastAdEntry entry = parseEntry(clip, adStartTime);
                    adStartTime += entry.getDuration();
                    entries.add(entry);
                } else if(category.equals(CONTENT_VOD) || category.equals(CONTENT_DEFAULT)) {
                    adStartTime += clip.getLong("duration");
                } else if(BuildConfig.DEBUG) {
                    throw new RuntimeException("VOD Ad category '" + category + "' not identified.");
                } else {
                    Log.e(TAG, "VOD Ad category '" + category + "' not identified.");
                }
            }
        }
        return new VastAdEntrySet(entries);
    }

    private VastAdEntry parseEntry(JSONObject clip, long adStartTime) throws JSONException {
        long duration = clip.getLong("duration");
        String title = clip.optString("title");
        String id = clip.optString("titleId");
        HashMap<AdEventType, VastImpression> logEntrySets = new HashMap<>();
        EventParser eventParser = new EventParser();

        JSONObject eventsJson = clip.getJSONObject("trackingEvents");
        for(String eventType : EventParser.eventTypes.keySet()) {
            JSONArray eventUrlsJson = eventsJson.optJSONArray(eventType);
            if(eventUrlsJson != null) {
                AdEventType type = eventParser.parse(eventType);
                ArrayList<URL> eventUrls = new ArrayList<>();
                for(int i = 0; i < eventUrlsJson.length(); i++) {
                    eventUrls.add(eventParser.parseEventUrl(eventUrlsJson.get(i).toString()));
                }
                logEntrySets.put(type, new VastImpression(type, eventUrls));
            }
        }
        return new VastAdEntry(id, title, adStartTime, duration, logEntrySets);
    }
}
