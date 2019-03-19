package com.redbeemedia.enigma.core.util;

import android.os.Handler;

//TODO generate this class and the IHandler interface with buildSrc
public class HandlerWrapper implements IHandler {
    private Handler handler;

    public HandlerWrapper(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean post(Runnable runnable) {
        return handler.post(runnable);
    }
}
