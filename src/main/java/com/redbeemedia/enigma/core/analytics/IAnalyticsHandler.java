package com.redbeemedia.enigma.core.analytics;

import org.json.JSONObject;

/**
 * Accepts analytics events.
 */
public interface IAnalyticsHandler {
    void onAnalytics(JSONObject jsonObject);
}
