package com.redbeemedia.enigma.core.time;

public class StopWatch implements IStopWatch {
    private final ITimeProvider timeProvider;
    private boolean started = false;
    private long start;
    private Duration stoppedTime = Duration.millis(0);

    public StopWatch(ITimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public void start() {
        started = true;
        start = timeProvider.getTime();
    }

    @Override
    public Duration stop() {
        if(!started) {
            throw new IllegalStateException("Not started");
        }
        this.stoppedTime = readTime();
        started = false;
        return stoppedTime;
    }

    @Override
    public Duration readTime() {
        if(started) {
            return Duration.millis(timeProvider.getTime()-start);
        } else {
            return stoppedTime;
        }
    }
}
