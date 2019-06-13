package com.redbeemedia.enigma.core.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestTaskFactory implements ITaskFactory {
    private final List<TestTaskFactory.TestTask> tasks = new ArrayList<>();
    private final long timeStep;

    public TestTaskFactory(long timeStep) {
        this.timeStep = timeStep;
    }

    public void letTimePass(long time) {
        if(time > timeStep) {
            letTimePass(time-timeStep);
            time = timeStep;
        }
        List<TestTaskFactory.TestTask> harvestedTasks = new ArrayList<>(tasks);
        tasks.clear();
        for(TestTaskFactory.TestTask task : harvestedTasks) {
            if(task.cancelled) {
                continue;
            }
            task.delayed = task.delayed-time;
            if(task.delayed > 0) {
                tasks.add(task);
            } else {
                task.runnable.run();
            }
        }
    }

    @Override
    public TestTaskFactory.TestTask newTask(Runnable runnable) {
        return new TestTaskFactory.TestTask(runnable);
    }

    public Collection<? extends ITask> getTasks() {
        return tasks;
    }

    private class TestTask implements ITask {
        private final Runnable runnable;
        private long delayed = 0;
        private boolean cancelled = false;

        public TestTask(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void start() throws TaskException {
            if(cancelled) {return;}

            if(!tasks.contains(this)) {
                tasks.add(this);
            }
        }

        @Override
        public void startDelayed(long delayMillis) throws TaskException {
            if(cancelled) {return;}

            this.delayed = delayMillis;
            if(!tasks.contains(this)) {
                tasks.add(this);
            }
        }

        @Override
        public void cancel(long joinMillis) throws TaskException {
            tasks.remove(this);
            cancelled = true;
        }
    }
}
