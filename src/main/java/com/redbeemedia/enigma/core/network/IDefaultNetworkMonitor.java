package com.redbeemedia.enigma.core.network;

import android.content.Context;

import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IDefaultNetworkMonitor extends INetworkMonitor {
    void start(Context context, ITaskFactoryProvider taskFactoryProvider);
}
