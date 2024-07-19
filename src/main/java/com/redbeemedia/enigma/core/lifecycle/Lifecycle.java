// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.lifecycle;

import com.redbeemedia.enigma.core.util.Collector;
import com.redbeemedia.enigma.core.util.IHandler;

public class Lifecycle<StartArgs, StopArgs> implements ILifecycle<StartArgs, StopArgs> {
    private volatile boolean onStartFired = false;
    private volatile boolean onStopFired = false;

    private final ListenerCollector listeners = new ListenerCollector();

    @Override
    public boolean addListener(ILifecycleListener<? super StartArgs, ? super StopArgs> listener) {
        return listeners.addListener(listener);
    }

    @Override
    public boolean addListener(ILifecycleListener<? super StartArgs, ? super StopArgs> listener, IHandler handler) {
        return listeners.addListener(listener, handler);
    }

    public void fireOnStart(StartArgs args) {
        if(onStartFired) {
            throw new IllegalStateException("OnStart already fired");
        } else {
            onStartFired = true;
        }
        listeners.onStart(args);
    }

    public void fireOnStop(StopArgs args) {
        if(onStopFired) {
            throw new IllegalStateException("OnStop already fired");
        } else {
            onStopFired = true;
        }
        listeners.onStop(args);
    }

    private class ListenerCollector extends Collector<ILifecycleListener<? super StartArgs, ? super StopArgs>> implements ILifecycleListener<StartArgs, StopArgs> {
        public ListenerCollector() {
            super((Class<ILifecycleListener<? super StartArgs, ? super StopArgs>>)(Class<?>) ILifecycleListener.class);
        }

        @Override
        public final void _dont_implement_ILifecycleListener___instead_extend_BaseLifecycleListener_() {
        }

        @Override
        public void onStart(StartArgs args) {
            listeners.forEach(listener -> listener.onStart(args));
        }

        @Override
        public void onStop(StopArgs args) {
            listeners.forEach(listener -> listener.onStop(args));
        }
    }
}
