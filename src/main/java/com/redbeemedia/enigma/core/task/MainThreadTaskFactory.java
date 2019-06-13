package com.redbeemedia.enigma.core.task;

import android.os.Handler;
import android.os.Looper;

import com.redbeemedia.enigma.core.util.HandlerWrapper;

public class MainThreadTaskFactory extends HandlerTaskFactory {
    public MainThreadTaskFactory() {
        super(new HandlerWrapper(new Handler(Looper.getMainLooper())));
    }
}
