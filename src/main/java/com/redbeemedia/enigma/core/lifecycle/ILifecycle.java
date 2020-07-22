package com.redbeemedia.enigma.core.lifecycle;

import com.redbeemedia.enigma.core.util.IHandler;

public interface ILifecycle<StartArgs,StopArgs> {
    boolean addListener(ILifecycleListener<? super StartArgs, ? super StopArgs> listener);
    boolean addListener(ILifecycleListener<? super StartArgs, ? super StopArgs> listener, IHandler handler);
}
