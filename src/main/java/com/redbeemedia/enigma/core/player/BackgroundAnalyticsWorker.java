package com.redbeemedia.enigma.core.player;

import static com.redbeemedia.enigma.core.analytics.OfflineAnalyticsHandler.OFFLINE_ANALYTICS_TAG;
import static com.redbeemedia.enigma.core.analytics.OfflineAnalyticsHandler.UPDATE_FREQUENCY_5_MIN;

import android.util.Log;

import com.redbeemedia.enigma.core.analytics.AnalyticsPlayResponseData;
import com.redbeemedia.enigma.core.analytics.OfflineAnalyticsHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundAnalyticsWorker extends Thread {

    public static final AtomicBoolean IS_STARTED = new AtomicBoolean(false);
    private OfflineAnalyticsHandler handler;

    public BackgroundAnalyticsWorker() {
    }

    @Override
    public void run() {
        if (!IS_STARTED.getAndSet(true)) {
            Log.d(OFFLINE_ANALYTICS_TAG, "Running Offline analytics background worker started");
            while (true) {
                try {
                    Log.d(OFFLINE_ANALYTICS_TAG, "**** Sending backgound events ****");
                    handler.sendData();
                    Thread.sleep(UPDATE_FREQUENCY_5_MIN);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setupHandler(ISession session, ITimeProvider itimeProvider) {
        AnalyticsPlayResponseData responseData = new AnalyticsPlayResponseData(new JSONObject(), "", null);
        handler = new OfflineAnalyticsHandler(session, itimeProvider, responseData);
    }
}
