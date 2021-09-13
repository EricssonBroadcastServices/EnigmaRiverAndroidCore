package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementations are capable of parsing Nowtilus related ads metadata.
 */
interface INowtilusParser {

    /**
     * Creates a <code>VastAdEntrySet</code> from xml.
     * @param resource An object containing the containing VAST data.
     * @return a vast entry set if the parsing was successful.
     */
    @Nullable VastAdEntrySet parseEntries(JSONObject resource) throws JSONException;

    class EventParser {

        /** Parse a string in to it's corresponding event type. */
        AdEventType parse(String eventTypeString) {
            if(eventTypeString == null || !eventTypes.containsKey(eventTypeString)) { return null; }
            return eventTypes.get(eventTypeString);
        }

        /** Available event types. */
        static final Map<String, AdEventType> eventTypes;
        static {
            HashMap <String, AdEventType> types = new HashMap<>();
            types.put("start", AdEventType.Start);
            types.put("firstQuartile", AdEventType.FirstQuartile);
            types.put("midpoint", AdEventType.MidPoint);
            types.put("thirdQuartile", AdEventType.ThirdQuartile);
            types.put("complete", AdEventType.Complete);
            eventTypes = types;
        }

        URL parseEventUrl(String urlString) {
            try {
                return new URL(urlString.replace("<![CDATA[", "").replace("]]>", "").trim());
            } catch(MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
