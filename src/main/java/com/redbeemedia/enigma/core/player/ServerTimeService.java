package com.redbeemedia.enigma.core.player;

import android.os.SystemClock;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.json.JsonObjectResponseHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.Repeater;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.IStopWatch;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.StopWatch;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/*package-protected*/ class ServerTimeService implements ITimeProvider {
    private static final long REFRESH_INTERVAL = 60000 * 30;
    private static final long EXPOSURE_DOWN_INTERVAL = 1000;

    private volatile long serverStartTime;
    private volatile Long localStartTime = null;
    private final ISession session;
    private final Repeater repeater;
    private final Repeater getFirstSyncRepeater;

    public ServerTimeService(ISession session, ITaskFactory taskFactory) {
        if(session == null) {
            throw new NullPointerException();
        }
        this.session = session;
        this.repeater = new Repeater(taskFactory, REFRESH_INTERVAL, new Runnable() {
            @Override
            public void run() {
                try {
                    syncWithServerTime();
                } catch (InterruptedException e) {
                    handleError(new UnexpectedError(e));
                }
            }
        });
        this.getFirstSyncRepeater = new Repeater(taskFactory, EXPOSURE_DOWN_INTERVAL, new Runnable() {
            @Override
            public void run() {
                repeater.executeNow();
            }
        });
    }

    protected void handleError(Error error) {
        //Ignore
        error.printStackTrace();
    }

    @Override
    public synchronized long getTime() {
        if(localStartTime == null) {
            throw new RuntimeException("Not synced!");
        }
        return this.serverStartTime + (getLocalTimeMillis() - this.localStartTime);
    }

    private synchronized void syncWithServerTime() throws InterruptedException {
        EnigmaRiverContext.getHttpHandler().doHttpBlocking(getTimeSyncUrl(), new AuthenticatedExposureApiCall("GET", session), new JsonObjectResponseHandler() {
            private static final String EPOCH_MILLIS = "epochMillis";

            @Override
            protected void onSuccess(JSONObject response) throws JSONException {
                if(response.has(EPOCH_MILLIS)) {
                    serverStartTime = response.optLong(EPOCH_MILLIS);
                    localStartTime = getLocalTimeMillis();
                    getFirstSyncRepeater.setEnabled(false);
                }
            }

            @Override
            protected void onError(Error error) {
                handleError(error);
                if(localStartTime == null) {
                    getFirstSyncRepeater.setEnabled(true);
                }
            }
        });
    }

    public void start(boolean await) {
        repeater.setEnabled(true);
        if(await) {
            IStopWatch timer = createStopWatch();
            timer.start();
            while(localStartTime == null && timer.readTime().inUnits(Duration.Unit.SECONDS) < 30) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            timer.stop();
            if(localStartTime == null) {
                throw new RuntimeException("Could not connect to backend");
            }
        }
    }

    private IStopWatch createStopWatch() {
        return new StopWatch(() -> getLocalTimeMillis());
    }

    public void stop() {
        repeater.setEnabled(false);
    }

    private URL getTimeSyncUrl() {
        try {
            return session.getBusinessUnit().getApiBaseUrl().append("/time").toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected long getLocalTimeMillis() {
        return SystemClock.uptimeMillis();
    }
}
