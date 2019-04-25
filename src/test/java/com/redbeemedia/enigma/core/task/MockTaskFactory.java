package com.redbeemedia.enigma.core.task;

public class MockTaskFactory implements ITaskFactory {
    @Override
    public ITask newTask(Runnable runnable) {
        return new ITask() {
            @Override
            public void start() throws IllegalStateException {

            }

            @Override
            public void startDelayed(long delayMillis) throws TaskException {

            }

            @Override
            public void cancel(long joinMillis) throws IllegalStateException {

            }
        };
    }
}
