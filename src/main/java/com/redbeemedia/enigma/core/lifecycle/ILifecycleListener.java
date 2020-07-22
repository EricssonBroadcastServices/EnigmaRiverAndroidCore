package com.redbeemedia.enigma.core.lifecycle;

import com.redbeemedia.enigma.core.util.IInternalListener;

public interface ILifecycleListener<StartArgs,StopArgs> extends IInternalListener {
    /**
     * <p>Inspired by {@code org.hamcrest.Matcher} from JUnit lib.</p>
     * <br>
     * <p style="margin-left: 25px; font-weight:bold;">It's easy to ignore JavaDoc, but a bit harder to ignore compile errors .</p>
     * <p style="margin-left: 50px">-- Hamcrest source</p>
     */
    @Deprecated
    void _dont_implement_ILifecycleListener___instead_extend_BaseLifecycleListener_();

    void onStart(StartArgs args);
    void onStop(StopArgs args);
}
