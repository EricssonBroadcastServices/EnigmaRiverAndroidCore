package com.redbeemedia.enigma.core.player;

import android.os.SystemClock;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.json.JsonObjectResponseHandler;
import com.redbeemedia.enigma.core.session.ISession;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Joao Coelho on 2018-01-03.
 * Cleaned by Matte on 2019-03-03
 */

//TODO refactor and replace this class with a more sensible design.
//TODO Also take more care regarding multithreading and synchronization.
//TODO And move to a better package.
/*package-protected*/ class LegacyTimeService implements Runnable {
    private static final long REFRESH_INTERVAL = 60000 * 30;
    private static final long EXPOSURE_DOWN_INTERVAL = 1000;

    private volatile long serverStartTime;
    private volatile long localStartTime;
    private volatile boolean running;
    private final ISession session;
    private volatile boolean stopped = false;

    public LegacyTimeService(ISession session) {
        this.session = session;
    }

    @Override
    public void run() {
        this.running = false;
        while (!stopped) {
            try {
                EnigmaRiverContext.getHttpHandler().doHttp(getTimeSyncUrl(), new AuthenticatedExposureApiCall("GET", session), new JsonObjectResponseHandler() {
                    @Override
                    protected void onSuccess(JSONObject response) throws JSONException {
                        serverStartTime = response.optLong("epochMillis");
                        localStartTime = getLocalTimeMillis();
                        running = true;
                    }

                    @Override
                    protected void onError(Error error) {
                        running = false;
                    }
                });
                if (running == false) {
                    Thread.sleep(EXPOSURE_DOWN_INTERVAL);
                } else {
                    Thread.sleep(REFRESH_INTERVAL);
                }
            } catch (InterruptedException e) {
                running = false;
                stopped = true;
                return;
            }
        }
    }

    public void stop() {
        this.stopped = true;
    }

    private URL getTimeSyncUrl() {
        try {
            return session.getApiBaseUrl().append("/time").toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long currentTime() {
        try {
            int n = 0;
            while (!this.running && n < 5000) {
                Thread.sleep(1);
                ++n;
            }
            if(!this.running) {
                return null;
            }
            if(this.running && serverStartTime > 0) {
                return this.serverStartTime + (getLocalTimeMillis() - this.localStartTime);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static long getLocalTimeMillis() {
        return SystemClock.uptimeMillis();
    }
}
