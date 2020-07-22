package com.redbeemedia.enigma.core.network;

import android.os.Handler;

public interface INetworkMonitor {
    boolean hasInternetAccess();
    boolean addListener(INetworkMonitorListener listener);
    boolean addListener(INetworkMonitorListener listener, Handler handler);
    boolean removeListener(INetworkMonitorListener listener);
}
