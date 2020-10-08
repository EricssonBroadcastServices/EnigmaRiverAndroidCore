package com.redbeemedia.enigma.core.context;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.redbeemedia.enigma.core.network.IDefaultNetworkMonitor;
import com.redbeemedia.enigma.core.network.INetworkMonitorListener;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.task.Repeater;
import com.redbeemedia.enigma.core.util.Collector;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.IValueChangedListener;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

/*package-protected*/ class DefaultNetworkMonitor implements IDefaultNetworkMonitor {
    private final NetworkMonitorCollector listeners = new NetworkMonitorCollector();

    private final OpenContainer<Boolean> internetAccess = new OpenContainer<>(false);

    @Override
    public synchronized void start(Context context, ITaskFactoryProvider taskFactoryProvider) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        synchronized (internetAccess) {
            internetAccess.value = checkInternetAvailable(connectivityManager);
        }
        
        LooperThread looperThread = new LooperThread();
        final IHandler listenerTrigger = new HandlerWrapper(new Handler(looperThread.getLooper()));
        if (Build.VERSION.SDK_INT >= 21) {
            NetworkRequest request = new NetworkRequest.Builder()
                    .build();
            connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                private void reevaulateConnectivity() {
                    boolean currentlyConnected = checkInternetAvailable(connectivityManager);
                    OpenContainerUtil.setValueSynchronized(internetAccess, currentlyConnected, (oldValue, newValue) -> {
                        listenerTrigger.post(() -> {
                            listeners.internetAccessChangedListener.onValueChanged(oldValue, newValue);
                        });
                    });
                }
                @Override
                public void onAvailable(Network network) {
                    reevaulateConnectivity();
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    reevaulateConnectivity();
                }

                @Override
                public void onLost(Network network) {
                    reevaulateConnectivity();
                }

                @Override
                public void onUnavailable() {
                    reevaulateConnectivity();
                }
            });
        } else {
            Repeater networkCheckRepeater = new Repeater(taskFactoryProvider.getTaskFactory(), 2000, () -> {
                boolean currentlyConnected = checkInternetAvailable(connectivityManager);

                OpenContainerUtil.setValueSynchronized(internetAccess, currentlyConnected, (oldValue, newValue) -> {
                    listenerTrigger.post(() -> {
                        listeners.internetAccessChangedListener.onValueChanged(oldValue, newValue);
                    });
                });
            });
            networkCheckRepeater.setEnabled(true);
        }
    }

    private boolean checkInternetAvailable(ConnectivityManager connectivityManager) {
        if(Build.VERSION.SDK_INT >= 21) {
            boolean currentlyConnected = false;
            Network[] allNetworks = connectivityManager.getAllNetworks();
            if(allNetworks != null) {
                for(Network network : allNetworks) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if(networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                        if(networkInfo != null && networkInfo.isConnected()) {
                            currentlyConnected = true;
                            break;
                        }
                    }
                }
            }
            return currentlyConnected;
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    @Override
    public boolean addListener(INetworkMonitorListener listener) {
        return listeners.addListener(listener);
    }

    @Override
    public boolean addListener(INetworkMonitorListener listener, Handler handler) {
        return addListener(listener, new HandlerWrapper(handler));
    }

    protected boolean addListener(INetworkMonitorListener listener, IHandler handler) {
        return listeners.addListener(listener, handler);
    }

    @Override
    public boolean removeListener(INetworkMonitorListener listener) {
        return listeners.removeListener(listener);
    }

    @Override
    public boolean hasInternetAccess() {
        return OpenContainerUtil.getValueSynchronized(internetAccess);
    }

    private static class NetworkMonitorCollector extends Collector<INetworkMonitorListener> {
        public final IValueChangedListener<Boolean> internetAccessChangedListener = (oldValue, newValue) -> NetworkMonitorCollector.this.forEach(listener -> listener.onInternetAccessChanged(newValue));

        public NetworkMonitorCollector() {
            super(INetworkMonitorListener.class);
        }
    }

    private static final class LooperThread {
        private final Looper looper;
        private volatile Looper looperResult = null;
        private volatile boolean gotException = false;

        public LooperThread() {
            Thread thread = new Thread(() -> {
                try {
                    Looper.prepare();
                    looperResult = Looper.myLooper();
                } catch (RuntimeException e) {
                    gotException = true;
                    throw e;
                } finally {
                    synchronized (LooperThread.this) { LooperThread.this.notifyAll(); }
                }
                Looper.loop();
            });
            thread.start();
            synchronized (this) {
                if (looperResult == null && gotException == false) {
                    try { wait(); }
                    catch (InterruptedException e) { throw new RuntimeException((e)); }
                }
            }
            if(gotException) {
                throw new RuntimeException("Failed to start looper thread.");
            }
            this.looper = looperResult;
        }

        public Looper getLooper() {
            return looper;
        }
    }
}
