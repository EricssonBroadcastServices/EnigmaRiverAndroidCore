package com.redbeemedia.enigma.core.lifecycle;

import com.redbeemedia.enigma.core.testutil.Counter;

import org.junit.Assert;
import org.junit.Test;

public class LifecycleTest {
    @Test
    public void testAllListenersCalledOnStartEvenIfException() {
        Lifecycle<String, String> lifecycle = new Lifecycle<>();

        final StringBuilder log = new StringBuilder();

        class LoggingListener extends BaseLifecycleListener<String,String> {
            private final String name;

            public LoggingListener(String name) {
                this.name = name;
            }

            @Override
            public void onStart(String s) {
                log.append("["+name+"::onStart("+s+")]");
            }

            @Override
            public void onStop(String s) {
                log.append("["+name+"::onStop("+s+")]");
            }
        }

        lifecycle.addListener(new LoggingListener("One"));
        lifecycle.addListener(new LoggingListener("Two"));
        lifecycle.addListener(new LoggingListener("Three") {
            @Override
            public void onStart(String s) {
                super.onStart(s);
                throw new RuntimeException("Fail in start");
            }
        });
        lifecycle.addListener(new LoggingListener("Four"));
        lifecycle.addListener(new LoggingListener("Five"));

        Assert.assertEquals("", log.toString());
        try {
            lifecycle.fireOnStart("X");
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertEquals("Fail in start", e.getMessage());
        }
        StringBuilder expectedLog = new StringBuilder();
        expectedLog.append("[One::onStart(X)]");
        expectedLog.append("[Two::onStart(X)]");
        expectedLog.append("[Three::onStart(X)]");
        expectedLog.append("[Four::onStart(X)]");
        expectedLog.append("[Five::onStart(X)]");
        Assert.assertEquals(expectedLog.toString(), log.toString());
    }

    @Test
    public void testAllListenersCalledOnStopEvenIfException() {
        Lifecycle<String, String> lifecycle = new Lifecycle<>();

        final StringBuilder log = new StringBuilder();

        class LoggingListener extends BaseLifecycleListener<String,String> {
            private final String name;

            public LoggingListener(String name) {
                this.name = name;
            }

            @Override
            public void onStart(String s) {
                log.append("["+name+"::onStart("+s+")]");
            }

            @Override
            public void onStop(String s) {
                log.append("["+name+"::onStop("+s+")]");
            }
        }

        lifecycle.addListener(new LoggingListener("One"));
        lifecycle.addListener(new LoggingListener("Two") {
            @Override
            public void onStop(String s) {
                throw new RuntimeException("Fail in stop");
            }
        });
        lifecycle.addListener(new LoggingListener("Three"));
        lifecycle.addListener(new LoggingListener("Four"));
        lifecycle.addListener(new LoggingListener("Five"));

        Assert.assertEquals("", log.toString());
        try {
            lifecycle.fireOnStop("Y");
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertEquals("Fail in stop", e.getMessage());
        }
        StringBuilder expectedLog = new StringBuilder();
        expectedLog.append("[One::onStop(Y)]");
        expectedLog.append("[Three::onStop(Y)]");
        expectedLog.append("[Four::onStop(Y)]");
        expectedLog.append("[Five::onStop(Y)]");
        Assert.assertEquals(expectedLog.toString(), log.toString());
    }

    @Test
    public void testGenericsCompatibility() {
        class BasicStartArg {
            public final String data;

            public BasicStartArg(String data) {
                this.data = data;
            }
        }

        class BasicStopArg {
            public final int number;

            public BasicStopArg(int number) {
                this.number = number;
            }
        }

        class ExtendedStartArg extends BasicStartArg {
            public final Float fraction;

            public ExtendedStartArg(String data, Float fraction) {
                super(data);
                this.fraction = fraction;
            }
        }

        class ExtendedStopArg extends BasicStopArg {
            public final boolean flag;

            public ExtendedStopArg(int number, boolean flag) {
                super(number);
                this.flag = flag;
            }
        }

        final StringBuilder log = new StringBuilder();

        class BasicListener extends BaseLifecycleListener<BasicStartArg, BasicStopArg> {

            @Override
            public void onStart(BasicStartArg basicStartArg) {
                log.append("[Start("+basicStartArg.data+")]");
            }

            @Override
            public void onStop(BasicStopArg basicStopArg) {
                log.append("[Stop("+basicStopArg.number+")]");
            }
        }

        class ExtendedListener extends BaseLifecycleListener<ExtendedStartArg, ExtendedStopArg> {

            @Override
            public void onStart(ExtendedStartArg extendedStartArg) {
                log.append("[EStart("+extendedStartArg.data+","+extendedStartArg.fraction+")]");
            }

            @Override
            public void onStop(ExtendedStopArg extendedStopArg) {
                log.append("[EStop("+extendedStopArg.number+","+extendedStopArg.flag+")]");
            }
        }

        Lifecycle<ExtendedStartArg, ExtendedStopArg> lifecycle = new Lifecycle<>();

        lifecycle.addListener(new BasicListener());
        lifecycle.addListener(new ExtendedListener());

        Assert.assertEquals("", log.toString());
        lifecycle.fireOnStart(new ExtendedStartArg("Song", 2f));
        Assert.assertEquals("[Start(Song)][EStart(Song,2.0)]", log.toString());

        log.setLength(0);

        Assert.assertEquals("", log.toString());
        lifecycle.fireOnStop(new ExtendedStopArg(12345, true));
        Assert.assertEquals("[Stop(12345)][EStop(12345,true)]", log.toString());
    }

    @Test
    public void testGenericsCompatibilityWithDowncast() {
        class BasicStartArg {
            public final String data;

            public BasicStartArg(String data) {
                this.data = data;
            }
        }

        class BasicStopArg {
            public final int number;

            public BasicStopArg(int number) {
                this.number = number;
            }
        }

        class ExtendedStartArg extends BasicStartArg {
            public final Float fraction;

            public ExtendedStartArg(String data, Float fraction) {
                super(data);
                this.fraction = fraction;
            }
        }

        class ExtendedStopArg extends BasicStopArg {
            public final boolean flag;

            public ExtendedStopArg(int number, boolean flag) {
                super(number);
                this.flag = flag;
            }
        }

        final StringBuilder log = new StringBuilder();

        class BasicListener extends BaseLifecycleListener<BasicStartArg, BasicStopArg> {

            @Override
            public void onStart(BasicStartArg basicStartArg) {
                log.append("[Start("+basicStartArg.data+")]");
            }

            @Override
            public void onStop(BasicStopArg basicStopArg) {
                log.append("[Stop("+basicStopArg.number+")]");
            }
        }

        class ExtendedListener extends BaseLifecycleListener<ExtendedStartArg, ExtendedStopArg> {

            @Override
            public void onStart(ExtendedStartArg extendedStartArg) {
                log.append("[EStart("+extendedStartArg.data+","+extendedStartArg.fraction+")]");
            }

            @Override
            public void onStop(ExtendedStopArg extendedStopArg) {
                log.append("[EStop("+extendedStopArg.number+","+extendedStopArg.flag+")]");
            }
        }

        Lifecycle<ExtendedStartArg, ExtendedStopArg> lifecycle = new Lifecycle<>();

        lifecycle.addListener(new BasicListener());
        lifecycle.addListener(new ExtendedListener());

        Lifecycle<? extends BasicStartArg, ? extends BasicStopArg> downcastLifecycle = lifecycle;
        downcastLifecycle.addListener(new BasicListener());

        Assert.assertEquals("", log.toString());
        lifecycle.fireOnStart(new ExtendedStartArg("Down", 23f));
        Assert.assertEquals("[Start(Down)][EStart(Down,23.0)][Start(Down)]", log.toString());
    }

    @Test
    public void testOnStartCallLimit() {
        Lifecycle<Void,Void> lifecycle = new Lifecycle<>();

        final Counter onStartCalled = new Counter();

        lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
            @Override
            public void onStart(Void aVoid) {
                onStartCalled.count();
            }

            @Override
            public void onStop(Void aVoid) {

            }
        });

        onStartCalled.assertNone();

        lifecycle.fireOnStart(null);

        onStartCalled.assertOnce();

        try {
            lifecycle.fireOnStart(null);
            Assert.fail("Expected exception");
        } catch (IllegalStateException e) {
            Assert.assertEquals("OnStart already fired",e.getMessage());
        }

        onStartCalled.assertOnce();
    }

    @Test
    public void testOnStopCallLimit() {
        Lifecycle<Void,Void> lifecycle = new Lifecycle<>();

        final Counter onStopCalled = new Counter();

        lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
            @Override
            public void onStart(Void aVoid) {
            }

            @Override
            public void onStop(Void aVoid) {
                onStopCalled.count();
            }
        });

        onStopCalled.assertNone();

        lifecycle.fireOnStop(null);

        onStopCalled.assertOnce();

        try {
            lifecycle.fireOnStop(null);
            Assert.fail("Expected exception");
        } catch (IllegalStateException e) {
            Assert.assertEquals("OnStop already fired",e.getMessage());
        }

        onStopCalled.assertOnce();
    }
}
