package com.redbeemedia.enigma.core.epg.request;

/*package-protected*/ class AbstractEpgRequest implements IEpgRequest {
    private final String channelId;
    private final long fromUtcMillis;
    private final long toUtcMillis;

    public AbstractEpgRequest(String channelId, long fromUtcMillis, long toUtcMillis) {
        this.channelId = channelId;
        this.fromUtcMillis = fromUtcMillis;
        this.toUtcMillis = toUtcMillis;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public long getFromUtcMillis() {
        return fromUtcMillis;
    }

    @Override
    public long getToUtcMillis() {
        return toUtcMillis;
    }
}
