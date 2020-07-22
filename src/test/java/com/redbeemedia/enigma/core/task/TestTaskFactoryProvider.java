package com.redbeemedia.enigma.core.task;

import java.util.Collection;

public class TestTaskFactoryProvider implements ITaskFactoryProvider {
    private final TestTaskFactory taskFactory;

    public TestTaskFactoryProvider(long timeStep) {
        this.taskFactory = new TestTaskFactory(timeStep);
    }

    @Override
    public ITaskFactory getTaskFactory() {
        return taskFactory;
    }

    @Override
    public ITaskFactory getMainThreadTaskFactory() {
        return taskFactory;
    }

    public void letTimePass(long time) {
        taskFactory.letTimePass(time);
    }

    public Collection<? extends ITask> getTasks() {
        return taskFactory.getTasks();
    }
}
