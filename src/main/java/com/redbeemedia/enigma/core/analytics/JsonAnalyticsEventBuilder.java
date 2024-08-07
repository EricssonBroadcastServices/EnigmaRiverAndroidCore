// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.analytics;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Builder interface for analytics events.
 */
/*package-protected*/ class JsonAnalyticsEventBuilder<E extends IAnalyticsEventType> implements IAnalyticsEventBuilder<E> {
    private JSONObject jsonObject = new JSONObject();

    public JsonAnalyticsEventBuilder(String eventType, long timestamp) throws JSONException {
        jsonObject.put("EventType", eventType);
        jsonObject.put("Timestamp", timestamp);
    }

    @Override
    public <T> void addData(IEventProperty<? super E,T> property, T value) throws JSONException {
        if(value != null || !property.skipIfNull()) {
            jsonObject.put(property.getName(), value);
        }
    }

    @Override
    public JSONObject build() {
        return jsonObject;
    }
}
