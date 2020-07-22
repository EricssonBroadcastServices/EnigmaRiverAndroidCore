package com.redbeemedia.enigma.core.network;

import com.redbeemedia.enigma.core.util.IInternalListener;

public interface INetworkMonitorListener extends IInternalListener {
    /**
     * <p>Inspired by {@code org.hamcrest.Matcher} from JUnit lib.</p>
     * <br>
     * <p style="margin-left: 25px; font-weight:bold;">It's easy to ignore JavaDoc, but a bit harder to ignore compile errors .</p>
     * <p style="margin-left: 50px">-- Hamcrest source</p>
     */
    @Deprecated
    void _dont_implement_INetworkMonitorListener___instead_extend_BaseNetworkMonitorListener_();

    void onInternetAccessChanged(boolean internetAccess);
}
