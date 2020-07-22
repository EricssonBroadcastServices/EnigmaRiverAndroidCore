package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.task.MainThreadTaskFactory;

/*package-protected*/ class DefaultTaskFactoryProvider implements ITaskFactoryProvider {
    private ITaskFactory defaultTaskFactory;

    public DefaultTaskFactoryProvider(ITaskFactory defaultTaskFactory) {
        this.defaultTaskFactory = defaultTaskFactory;
    }

    @Override
    public ITaskFactory getTaskFactory() {
        return defaultTaskFactory;
    }

    public void setTaskFactory(ITaskFactory defaultTaskFactory) {
        this.defaultTaskFactory = defaultTaskFactory;
    }

    @Override
    public ITaskFactory getMainThreadTaskFactory() {
        return new MainThreadTaskFactory();
    }
}
