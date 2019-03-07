package com.redbeemedia.enigma.core.task;

public interface ITask {
    void start() throws TaskException;
    void cancel(long joinMillis) throws TaskException;
}
