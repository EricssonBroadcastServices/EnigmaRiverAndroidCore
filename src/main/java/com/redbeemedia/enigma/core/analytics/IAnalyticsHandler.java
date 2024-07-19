// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.analytics;

import org.json.JSONObject;

/**
 * Accepts analytics events.
 */
public interface IAnalyticsHandler {
    void onAnalytics(JSONObject jsonObject);
}
