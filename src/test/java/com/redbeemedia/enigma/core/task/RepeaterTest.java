package com.redbeemedia.enigma.core.task;

import com.redbeemedia.enigma.core.testutil.Counter;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RepeaterTest {
    @Test
    public void test() {
        TestTaskFactory taskFactory = new TestTaskFactory(50);
        final Counter runCalled = new Counter();
        Repeater repeater = new Repeater(taskFactory, 100, new Runnable() {
            @Override
            public void run() {
                runCalled.count();
            }
        });
        runCalled.assertNone();
        int expectedRunTimes = 0;

        taskFactory.letTimePass(5000);//Let 5 sec pass
        runCalled.assertNone();

        repeater.executeNow();
        taskFactory.letTimePass(2000);//Let 2 sec pass
        expectedRunTimes += 1;
        runCalled.assertCount(expectedRunTimes);

        repeater.setEnabled(true);
        taskFactory.letTimePass(999);//Let 0.999 sec pass
        repeater.setEnabled(false);
        expectedRunTimes += 10;
        runCalled.assertCount(expectedRunTimes);

        taskFactory.letTimePass(500);//Let half a sec pass
        runCalled.assertCount(expectedRunTimes);

        repeater.setEnabled(true);
        taskFactory.letTimePass(150);
        expectedRunTimes += 2;
        runCalled.assertCount(expectedRunTimes);
        repeater.executeNow();
        expectedRunTimes += 1;
        runCalled.assertCount(expectedRunTimes);
        taskFactory.letTimePass(150);
        expectedRunTimes += 1;
        runCalled.assertCount(expectedRunTimes);

        taskFactory.letTimePass(301);
        expectedRunTimes += 3;
        runCalled.assertCount(expectedRunTimes);

        repeater.setEnabled(true);
        taskFactory.letTimePass(1);
        runCalled.assertCount(expectedRunTimes);

        repeater.setEnabled(false);
        repeater.executeNow();
        taskFactory.letTimePass(10000);
        expectedRunTimes += 1;
        runCalled.assertCount(expectedRunTimes);

        Assert.assertEquals(0, taskFactory.tasks.size());
    }

    private static class TestTaskFactory implements ITaskFactory {
        private final List<TestTask> tasks = new ArrayList<>();
        private final long timeStep;

        public TestTaskFactory(long timeStep) {
            this.timeStep = timeStep;
        }

        public void letTimePass(long time) {
            if(time > timeStep) {
                letTimePass(time-timeStep);
                time = timeStep;
            }
            List<TestTask> harvestedTasks = new ArrayList<>(tasks);
            tasks.clear();
            for(TestTask task : harvestedTasks) {
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
        public TestTask newTask(Runnable runnable) {
            return new TestTask(runnable);
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
}
