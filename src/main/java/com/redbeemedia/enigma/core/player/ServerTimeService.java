package com.redbeemedia.enigma.core.player;

import android.os.SystemClock;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.http.ExposureApiCall;
import com.redbeemedia.enigma.core.json.JsonObjectResponseHandler;
import com.redbeemedia.enigma.core.lifecycle.BaseLifecycleListener;
import com.redbeemedia.enigma.core.network.BaseNetworkMonitorListener;
import com.redbeemedia.enigma.core.network.INetworkMonitor;
import com.redbeemedia.enigma.core.network.INetworkMonitorListener;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.Repeater;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.IStopWatch;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.StopWatch;
import com.redbeemedia.enigma.core.time.SystemBootTimeProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/*package-protected*/ class ServerTimeService implements ITimeProvider {
    private static final long REFRESH_INTERVAL = 60000 * 30;
    private static final long EXPOSURE_DOWN_INTERVAL = 1000;

    private volatile long serverStartTime;
    private volatile Long localStartTime = null;
    private final IBusinessUnit businessUnit;
    private final Repeater repeater;
    private final Repeater getFirstSyncRepeater;
    private final ITimeProvider fallbackTimeProvider;

    public ServerTimeService(IBusinessUnit businessUnit, ITaskFactory taskFactory, EnigmaPlayer.EnigmaPlayerLifecycle lifecycle) {
        if(businessUnit == null) {
            throw new NullPointerException();
        }
        this.businessUnit = businessUnit;
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
        this.fallbackTimeProvider = newFallbackTimeProvider();

        lifecycle.addListener(new BaseLifecycleListener<Object, Object>() {
            private INetworkMonitor networkMonitor = EnigmaRiverContext.getNetworkMonitor();
            private INetworkMonitorListener networkMonitorListener = new BaseNetworkMonitorListener() {
                @Override
                public void onInternetAccessChanged(boolean internetAccess) {
                    if(internetAccess) {
                        ServerTimeService.this.start(false);
                    } else {
                        ServerTimeService.this.stop();
                    }
                }
            };

            @Override
            public void onStart(Object o) {
                if(networkMonitor.hasInternetAccess()) {
                    ServerTimeService.this.start(false);
                }
                networkMonitor.addListener(networkMonitorListener);
            }

            @Override
            public void onStop(Object o) {
                networkMonitor.removeListener(networkMonitorListener);
                ServerTimeService.this.stop();
            }
        });
    }

    protected ITimeProvider newFallbackTimeProvider() {
        return new SystemBootTimeProvider();
    }

    protected void handleError(EnigmaError error) {
        //Ignore
        error.printStackTrace();
    }

    @Override
    public synchronized long getTime() {
        if(localStartTime == null) {
            return fallbackTimeProvider.getTime();
        }
        return this.serverStartTime + (getLocalTimeMillis() - this.localStartTime);
    }

    private synchronized void syncWithServerTime() throws InterruptedException {
        EnigmaRiverContext.getHttpHandler().doHttpBlocking(getTimeSyncUrl(), new ExposureApiCall("GET"), new JsonObjectResponseHandler() {
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
            protected void onError(EnigmaError error) {
                handleError(error);
                if(localStartTime == null) {
                    getFirstSyncRepeater.setEnabled(true);
                }
            }
        });
    }

    private void start(boolean await) {
        repeater.setEnabled(true);
        if(await) {
            if(!isReady(Duration.seconds(30))) {
                throw new RuntimeException("Could not connect to backend");
            }
        }
    }

    @Override
    public boolean isReady(Duration maxBlocktime) {
        if(!EnigmaRiverContext.getNetworkMonitor().hasInternetAccess()) {
            return true;
        }
        if(maxBlocktime == null) {
            throw new NullPointerException("maxBlocktime is null");
        }
        if(localStartTime != null) {
            return true;
        } else {
            IStopWatch timer = newStopWatch();
            timer.start();
            while(localStartTime == null && timer.readTime().inUnits(Duration.Unit.SECONDS) < maxBlocktime.inUnits(Duration.Unit.SECONDS)) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            timer.stop();
            return localStartTime != null;
        }
    }

    protected IStopWatch newStopWatch() {
        return new StopWatch(new ITimeProvider() {
            @Override
            public long getTime() {
                return getLocalTimeMillis();
            }

            @Override
            public boolean isReady(Duration maxBlocktime) {
                return true;
            }
        });
    }

    private void stop() {
        repeater.setEnabled(false);
    }

    private URL getTimeSyncUrl() {
        try {
            return businessUnit.getApiBaseUrl().append("/time").toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected long getLocalTimeMillis() {
        return SystemClock.uptimeMillis();
    }
}
