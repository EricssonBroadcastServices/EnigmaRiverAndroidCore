package com.redbeemedia.enigma.core.analytics;

import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ interface IAnalyticsEventBuilder<E extends IAnalyticsEventType> {
    <T> void addData(IEventProperty<E,T> property, T value) throws JSONException;
    JSONObject build();
}
