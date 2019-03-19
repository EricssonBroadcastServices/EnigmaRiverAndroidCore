package com.redbeemedia.enigma.core.task;

public interface ITaskFactory {
    ITask newTask(Runnable runnable);
}
