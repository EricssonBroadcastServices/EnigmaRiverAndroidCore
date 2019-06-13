package com.redbeemedia.enigma.core.time;

public interface IStopWatch {
    void start();
    Duration stop();
    Duration readTime();
}
