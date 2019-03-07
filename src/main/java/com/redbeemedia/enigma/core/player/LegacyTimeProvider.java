package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.time.ITimeProvider;

/*package-protected*/ class LegacyTimeProvider implements ITimeProvider {
    private final LegacyTimeService task;
    private ITask taskInstance;

    public LegacyTimeProvider(ISession session) {
        this.task = new LegacyTimeService(session);
        try {
            this.startThread(EnigmaRiverContext.getTaskFactory());
        } catch (TaskException e) {
            throw new RuntimeException(e);
        }
    }

    private LegacyTimeProvider startThread(ITaskFactory taskFactory) throws TaskException {
        synchronized (taskFactory) {
            this.taskInstance = taskFactory.newTask(task);
            this.taskInstance.start();
        }
        return this;
    }

    @Override
    public long getTime() {
        Long currentTime = task.currentTime();
        if(currentTime != null) {
            return currentTime.longValue();
        } else {
            throw new IllegalStateException("Could not determine server time. Thread might not have been started, or can't reach server.");
        }
    }

    public void release() {
        try {
            taskInstance.cancel(100);
        } catch (TaskException e) {
            throw new RuntimeException(e);
        }
    }
}
