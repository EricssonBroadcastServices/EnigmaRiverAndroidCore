package com.redbeemedia.enigma.core.analytics;

public interface IBufferingAnalyticsHandler extends IAnalyticsHandler {
    void init() throws AnalyticsException, InterruptedException;
    void sendData() throws AnalyticsException, InterruptedException;
}
