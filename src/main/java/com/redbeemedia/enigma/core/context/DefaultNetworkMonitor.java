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

        android.util.Log.d("freezelog", "DefaultNetworkMonitor 1");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        {
            android.util.Log.d("freezelog", "DefaultNetworkMonitor 2");
            synchronized (internetAccess) {
                android.util.Log.d("freezelog", "DefaultNetworkMonitor 3");
                internetAccess.value = checkInternetAvailable(connectivityManager);
                android.util.Log.d("freezelog", "DefaultNetworkMonitor 4");
            }
        }

        LooperThread looperThread = new LooperThread();
        android.util.Log.d("freezelog", "DefaultNetworkMonitor 5");
        final IHandler listenerTrigger = new HandlerWrapper(new Handler(looperThread.getLooper()));
        android.util.Log.d("freezelog", "DefaultNetworkMonitor 6");
        if (Build.VERSION.SDK_INT >= 21) {
            android.util.Log.d("freezelog", "DefaultNetworkMonitor 7");
            NetworkRequest request  = new NetworkRequest.Builder()
                    .build();
            android.util.Log.d("freezelog", "DefaultNetworkMonitor 8");
            connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                private void reevaulateConnectivity() {
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 1");
                    boolean currentlyConnected = checkInternetAvailable(connectivityManager);
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 2");
                    OpenContainerUtil.setValueSynchronized(internetAccess, currentlyConnected, (oldValue, newValue) -> {
                        android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 3");
                        listenerTrigger.post(() -> {
                            android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 4");
                            listeners.internetAccessChangedListener.onValueChanged(oldValue, newValue);
                            android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 5");
                        });
                        android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 6");
                    });
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 7");
                }
                @Override
                public void onAvailable(Network network) {
                    reevaulateConnectivity();
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 8");
                    reevaulateConnectivity();
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 9");
                }

                @Override
                public void onLost(Network network) {
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 10");
                    reevaulateConnectivity();
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 11");
                }

                @Override
                public void onUnavailable() {
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 12");
                    reevaulateConnectivity();
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor connectivityManager 13");
                }
            });
            android.util.Log.d("freezelog", "DefaultNetworkMonitor 9");
        } else {
            android.util.Log.d("freezelog", "DefaultNetworkMonitor -- x");
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
        android.util.Log.d("freezelog", "DefaultNetworkMonitor checkInternetAvailable 1");
        if(Build.VERSION.SDK_INT >= 21) {
            boolean currentlyConnected = false;
            android.util.Log.d("freezelog", "DefaultNetworkMonitor checkInternetAvailable 2");
            Network[] allNetworks = connectivityManager.getAllNetworks();
            android.util.Log.d("freezelog", "DefaultNetworkMonitor checkInternetAvailable 3");
            if(allNetworks != null) {
                android.util.Log.d("freezelog", "DefaultNetworkMonitor checkInternetAvailable 3");
                for(Network network : allNetworks) {
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor checkInternetAvailable network: " + network.toString());
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    android.util.Log.d("freezelog", "DefaultNetworkMonitor checkInternetAvailable network: " + networkCapabilities != null ? networkCapabilities.toString() : "null");
                    if(networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        android.util.Log.d("freezelog", "DefaultNetworkMonitor checkInternetAvailable x");
                        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                        android.util.Log.d("freezelog", "DefaultNetworkMonitor checkInternetAvailable y");
                        if(networkInfo != null && networkInfo.isConnected()) {
                            android.util.Log.d("freezelog", "DefaultNetworkMonitor checkInternetAvailable z");
                            currentlyConnected = true;
                            break;
                        }
                    }
                }
            }
            return currentlyConnected;
        } else {
            android.util.Log.d("freezelog", "DefaultNetworkMonitor checkInternetAvailable --");
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

        public LooperThread() {
            android.util.Log.d("freezelog", "LooperThread 1");
            final Looper[] looperResult = new Looper[]{null};
            final boolean[] gotException = new boolean[]{false};
            android.util.Log.d("freezelog", "LooperThread 2");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        android.util.Log.d("freezelog", "Looper.run 1");
                        Looper.prepare();
                        android.util.Log.d("freezelog", "Looper.run 2");
                        looperResult[0] = Looper.myLooper();
                        android.util.Log.d("freezelog", "Looper.run 3");
                    } catch (RuntimeException e) {
                        android.util.Log.d("freezelog", "Looper.run exception: " + e.getMessage());
                        gotException[0] = true;
                        throw e;
                    }
                    android.util.Log.d("freezelog", "Looper.run 4");
                    Looper.loop();
                    android.util.Log.d("freezelog", "Looper.run 5");
                }
            });
            thread.start();
            android.util.Log.d("freezelog", "LooperThread 3");
            int i = 0;
            while (looperResult[0] == null && !gotException[0]) {
                if (i % 50 == 0) {
                    android.util.Log.d("freezelog", "LooperThread while: " + Integer.toString(i));
                }
                i++;
            }
            android.util.Log.d("freezelog", "LooperThread 4");
            if(gotException[0]) {
                android.util.Log.d("freezelog", "LooperThread: exception");
                throw new RuntimeException("Failed to start looper thread.");
            }
            android.util.Log.d("freezelog", "LooperThread 5");
            this.looper = looperResult[0];
            android.util.Log.d("freezelog", "LooperThread 6");
        }

        public Looper getLooper() {
            return looper;
        }
    }
}
