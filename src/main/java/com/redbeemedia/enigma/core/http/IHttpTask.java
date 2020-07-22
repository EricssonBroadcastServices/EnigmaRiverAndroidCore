package com.redbeemedia.enigma.core.http;

public interface IHttpTask {
    boolean isDone();
    void cancel(long joinMillis);
}
