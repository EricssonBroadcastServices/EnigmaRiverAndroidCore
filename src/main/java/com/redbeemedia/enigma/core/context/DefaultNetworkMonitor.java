// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

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
import com.redbeemedia.enigma.core.network.INetworkMonitor;
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
    private final OpenContainer<String> connectionType = new OpenContainer<>(INetworkMonitor.CONNECTION_TYPE_UNKNOWN);

    @Override
    public synchronized void start(Context context, ITaskFactoryProvider taskFactoryProvider) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        synchronized (internetAccess) {
            internetAccess.value = checkInternetAvailable(connectivityManager);
            connectionType.value = checkConnectionType(connectivityManager);
        }

        LooperThread looperThread = new LooperThread();
        final IHandler listenerTrigger = new HandlerWrapper(new Handler(looperThread.getLooper()));
        if (Build.VERSION.SDK_INT >= 21) {
            NetworkRequest request = new NetworkRequest.Builder()
                    .build();
            connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                private void reevaulateConnectivity() {
                    boolean currentlyConnected = checkInternetAvailable(connectivityManager);
                    String currentConnectionType = checkConnectionType(connectivityManager);
                    OpenContainerUtil.setValueSynchronized(internetAccess, currentlyConnected, (oldValue, newValue) -> {
                        listenerTrigger.post(() -> {
                            listeners.internetAccessChangedListener.onValueChanged(oldValue, newValue);
                        });
                    });
                    OpenContainerUtil.setValueSynchronized(connectionType, currentConnectionType,
                            (oldValue, newValue) -> {
                                listenerTrigger.post(() -> {
                                    listeners.connectionTypeChangedListener.onValueChanged(oldValue, newValue);
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
                String currentConnectionType = checkConnectionType(connectivityManager);

                OpenContainerUtil.setValueSynchronized(internetAccess, currentlyConnected, (oldValue, newValue) -> {
                    listenerTrigger.post(() -> {
                        listeners.internetAccessChangedListener.onValueChanged(oldValue, newValue);
                    });
                });
                OpenContainerUtil.setValueSynchronized(connectionType, currentConnectionType, (oldValue, newValue) -> {
                    listenerTrigger.post(() -> {
                        listeners.connectionTypeChangedListener.onValueChanged(oldValue, newValue);
                    });
                });
            });
            networkCheckRepeater.setEnabled(true);
        }
    }

    private boolean checkInternetAvailable(ConnectivityManager connectivityManager) {
        if (connectivityManager == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            boolean currentlyConnected = false;
            Network[] allNetworks = connectivityManager.getAllNetworks();
            if (allNetworks != null) {
                for (Network network : allNetworks) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if (networkCapabilities != null
                            && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                        if (networkInfo != null && networkInfo.isConnected()) {
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

    private String checkConnectionType(ConnectivityManager connectivityManager) {
        if (connectivityManager == null) {
            return INetworkMonitor.CONNECTION_TYPE_UNKNOWN;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Network[] allNetworks = connectivityManager.getAllNetworks();
            if (allNetworks != null) {
                for (Network network : allNetworks) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if (networkCapabilities != null
                            && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                        if (networkInfo != null && networkInfo.isConnected()) {
                            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                return INetworkMonitor.CONNECTION_TYPE_WIFI;
                            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                return INetworkMonitor.CONNECTION_TYPE_CELLULAR;
                            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                                return INetworkMonitor.CONNECTION_TYPE_ETHERNET;
                            }
                        }
                    }
                }
            }
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return INetworkMonitor.CONNECTION_TYPE_WIFI;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return INetworkMonitor.CONNECTION_TYPE_CELLULAR;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    return INetworkMonitor.CONNECTION_TYPE_ETHERNET;
                }
            }
        }
        return INetworkMonitor.CONNECTION_TYPE_NONE;
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

    @Override
    public String getConnectionType() {
        return OpenContainerUtil.getValueSynchronized(connectionType);
    }

    private static class NetworkMonitorCollector extends Collector<INetworkMonitorListener> {
        public final IValueChangedListener<Boolean> internetAccessChangedListener = (oldValue,
                newValue) -> NetworkMonitorCollector.this
                        .forEach(listener -> listener.onInternetAccessChanged(newValue));
        public final IValueChangedListener<String> connectionTypeChangedListener = (oldValue,
                newValue) -> NetworkMonitorCollector.this
                        .forEach(listener -> listener.onConnectionTypeChanged(newValue));

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
                    synchronized (LooperThread.this) {
                        LooperThread.this.notifyAll();
                    }
                }
                Looper.loop();
            });
            thread.start();
            synchronized (this) {
                if (looperResult == null && gotException == false) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException((e));
                    }
                }
            }
            if (gotException) {
                throw new RuntimeException("Failed to start looper thread.");
            }
            this.looper = looperResult;
        }

        public Looper getLooper() {
            return looper;
        }
    }
}
