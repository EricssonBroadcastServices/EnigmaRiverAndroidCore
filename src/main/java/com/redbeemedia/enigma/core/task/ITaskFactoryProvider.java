package com.redbeemedia.enigma.core.task;

public interface ITaskFactoryProvider {
    ITaskFactory getTaskFactory();
    ITaskFactory getMainThreadTaskFactory();
}
