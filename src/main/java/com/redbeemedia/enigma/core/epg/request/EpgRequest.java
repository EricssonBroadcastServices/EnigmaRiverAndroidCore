package com.redbeemedia.enigma.core.epg.request;

public class EpgRequest extends AbstractEpgRequest {
    public EpgRequest(String channelId, long fromUtcMillis, long toUtcMillis) {
        super(channelId, fromUtcMillis, toUtcMillis);
    }
}
