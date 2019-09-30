package com.redbeemedia.enigma.core.time;

public interface ITimeProvider {
    long getTime();
    boolean isReady(Duration maxBlocktime);
}
