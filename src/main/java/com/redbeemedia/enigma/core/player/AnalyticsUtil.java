/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.http.SimpleHttpCall;
import com.redbeemedia.enigma.core.json.StringResponseHandler;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.function.IConsumer;

import java.util.Date;

public class AnalyticsUtil {
    private static final String URL_SERVER_TIME_PROVIDER = "https://time.akamai.com/";

    public static void setDeviceUtcTimeDifference(IConsumer<Long> onDeviceUtcTimeDifferenceAvailable) {
        try {
            UrlPath url = new UrlPath(URL_SERVER_TIME_PROVIDER);
            SimpleHttpCall apiCall = new SimpleHttpCall("GET");
            EnigmaRiverContext.getHttpHandler().doHttp(url.toURL(), apiCall, new StringResponseHandler() {

                @Override
                public void onError(EnigmaError error) {
                    onDeviceUtcTimeDifferenceAvailable.accept(0L);
                }

                @Override
                public void onSuccess(String serverUtcTimeStr) {
                    try {
                        long serverUtcTime = Long.parseLong(serverUtcTimeStr) * 1000;
                        long deviceTime = new Date().getTime();
                        long networkTime = 500L;
                        onDeviceUtcTimeDifferenceAvailable.accept(deviceTime - serverUtcTime - networkTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // ignore and set device time difference as 0
                        onDeviceUtcTimeDifferenceAvailable.accept(0L);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            onDeviceUtcTimeDifferenceAvailable.accept(0L);
            // IGNORE
        }
    }
}
