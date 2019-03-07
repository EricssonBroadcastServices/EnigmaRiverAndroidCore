package com.redbeemedia.enigma.core.time;

public class MockTimeProvider implements ITimeProvider {
    private long currentTime;

    public MockTimeProvider() {
        this(0);
    }

    public MockTimeProvider(long initial) {
        this.currentTime = initial;
    }

    public void addTime(long increment) {
        currentTime += increment;
    }

    @Override
    public long getTime() {
        return currentTime;
    }
}
