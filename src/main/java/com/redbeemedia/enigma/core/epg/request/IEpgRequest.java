package com.redbeemedia.enigma.core.epg.request;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 */
public interface IEpgRequest {
    String getChannelId();
    long getFromUtcMillis();
    long getToUtcMillis();
}
