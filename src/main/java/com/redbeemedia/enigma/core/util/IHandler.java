package com.redbeemedia.enigma.core.util;

public interface IHandler {
    boolean post(Runnable runnable);
    boolean postDelayed(Runnable runnable, long delayMillis);
}
