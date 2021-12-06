package com.redbeemedia.enigma.core.analytics;

public interface IBufferingAnalyticsHandler extends IAnalyticsHandler {
    void sendData() throws AnalyticsException, InterruptedException;
}
