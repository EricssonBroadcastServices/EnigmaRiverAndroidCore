// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.analytics;

import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ interface IAnalyticsEventBuilder<E extends IAnalyticsEventType> {
    <T> void addData(IEventProperty<? super E,T> property, T value) throws JSONException;
    JSONObject build();
}
