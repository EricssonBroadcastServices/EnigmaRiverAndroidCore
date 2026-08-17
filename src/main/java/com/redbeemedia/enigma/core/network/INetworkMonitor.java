// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.network;

import android.os.Handler;

public interface INetworkMonitor {
    String CONNECTION_TYPE_WIFI = "WiFi";
    String CONNECTION_TYPE_CELLULAR = "Cellular";
    String CONNECTION_TYPE_ETHERNET = "Ethernet";
    String CONNECTION_TYPE_NONE = "None";
    String CONNECTION_TYPE_UNKNOWN = "Unknown";

    boolean hasInternetAccess();

    String getConnectionType();

    boolean addListener(INetworkMonitorListener listener);

    boolean addListener(INetworkMonitorListener listener, Handler handler);

    boolean removeListener(INetworkMonitorListener listener);
}
