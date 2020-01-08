package com.redbeemedia.enigma.core.util;

import com.redbeemedia.enigma.core.testutil.ReflectionUtil;
import com.redbeemedia.enigma.core.testutil.thread.Interruptor;
import com.redbeemedia.enigma.core.testutil.thread.ThreadHalter;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class CollectorTest {
    @Test
    public void testForEach() {
        StringBuilder log = new StringBuilder();
        Collector<ILoggingMockListener> stringCollector = new Collector<>(ILoggingMockListener.class);
        LoggingMockListener str1 = new LoggingMockListener("One");
        LoggingMockListener str2 = new LoggingMockListener("Two");
        LoggingMockListener str3 = new LoggingMockListener("Three");
        Assert.assertTrue(stringCollector.addListener(str1));
        stringCollector.forEach(listener -> listener.writeToLog(log, (String s) -> s));
        Assert.assertEquals("One", log.toString());
        Assert.assertTrue(stringCollector.addListener(str2));
        Assert.assertTrue(stringCollector.addListener(str3));
        stringCollector.forEach(listener -> listener.writeToLog(log, (String s) -> s.toUpperCase(Locale.ENGLISH)));
        Assert.assertEquals("OneONETWOTHREE", log.toString());
        Assert.assertTrue(stringCollector.removeListener(str2));
        Assert.assertFalse(stringCollector.removeListener(str2));
        stringCollector.forEach(listener -> listener.writeToLog(log, (String s) -> s.toLowerCase(Locale.ENGLISH)));
        Assert.assertEquals("OneONETWOTHREEonethree", log.toString());
        Assert.assertTrue(stringCollector.removeListener(str3));
        Assert.assertTrue(stringCollector.removeListener(str1));
        stringCollector.forEach(listener -> log.setLength(0));
        Assert.assertEquals("OneONETWOTHREEonethree", log.toString());
    }

    @Test
    public void testForEachWithExceptions() {
        StringBuilder log = new StringBuilder();
        Collector<ILoggingMockListener> stringCollector = new Collector<>(ILoggingMockListener.class);
        LoggingMockListener str1 = new LoggingMockListener("One");
        LoggingMockListener str2 = new LoggingMockListener("Two") {
            @Override
            public void writeToLog(StringBuilder log, IStringTransform stringTransform) {
                throw new RuntimeException("Fail in listener!");
            }
        };
        final boolean[] exceptionInThree = new boolean[]{false};
        LoggingMockListener str3 = new LoggingMockListener("Three") {
            @Override
            public void writeToLog(StringBuilder log, IStringTransform stringTransform) {
                if(exceptionInThree[0]) {
                    throw new RuntimeException("Fail from three");
                }
                super.writeToLog(log, stringTransform);
            }
        };
        Assert.assertTrue(stringCollector.addListener(str1));
        stringCollector.forEach(listener -> listener.writeToLog(log, (String s) -> s));
        Assert.assertEquals("One", log.toString());
        Assert.assertTrue(stringCollector.addListener(str2));
        Assert.assertTrue(stringCollector.addListener(str3));
        try {
            stringCollector.forEach(listener -> listener.writeToLog(log, (String s) -> s.toUpperCase(Locale.ENGLISH)));
            Assert.fail("Expected runtime exception");
        } catch (AssertionError e) {
            throw e;
        } catch (RuntimeException e) {
            Assert.assertEquals("Fail in listener!", e.getMessage());
        }
        Assert.assertEquals("OneONETHREE", log.toString());
        exceptionInThree[0] = true;
        try {
            stringCollector.forEach(listener -> listener.writeToLog(log, (String s) -> s.toUpperCase(Locale.ENGLISH)));
            Assert.fail("Expected runtime exception");
        } catch (AssertionError e) {
            throw e;
        } catch (RuntimeException e) {
            Assert.assertEquals("Fail in listener!", e.getMessage());
            Throwable[] supressed = e.getSuppressed();
            Assert.assertEquals(1, supressed.length);
            Assert.assertEquals("Fail from three", supressed[0].getMessage());
        }
        exceptionInThree[0] = false;
        Assert.assertEquals("OneONETHREEONE", log.toString());
        Assert.assertTrue(stringCollector.removeListener(str2));
        Assert.assertFalse(stringCollector.removeListener(str2));
        stringCollector.forEach(listener -> listener.writeToLog(log, (String s) -> s.toLowerCase(Locale.ENGLISH)));
        Assert.assertEquals("OneONETHREEONEonethree", log.toString());
        Assert.assertTrue(stringCollector.removeListener(str3));
        Assert.assertTrue(stringCollector.removeListener(str1));
        stringCollector.forEach(listener -> log.setLength(0));
        Assert.assertEquals("OneONETHREEONEonethree", log.toString());
    }

    @Test
    public void testCollectorWithHandlers() {
        StringBuilder log = new StringBuilder();
        Collector<ILoggingMockListener> stringCollector = new Collector<>(ILoggingMockListener.class);
        LoggingMockListener str1 = new LoggingMockListener("One");
        LoggingMockListener str2 = new LoggingMockListener("Two");
        LoggingMockListener str3 = new LoggingMockListener("Three");
        Assert.assertTrue(stringCollector.addListener(str1));
        stringCollector.forEach(listener -> listener.writeToLog(log, (String s) -> s));
        Assert.assertEquals("One", log.toString());
        MockHandler mockHandler = new MockHandler();
        Assert.assertTrue(stringCollector.addListener(str2, mockHandler));
        Assert.assertTrue(stringCollector.addListener(str3));
        stringCollector.forEach(listener -> listener.writeToLog(log, (String s) -> s.toUpperCase(Locale.ENGLISH)));
        Assert.assertEquals(1, mockHandler.runnables.size());
        Assert.assertEquals("OneONETHREE", log.toString());
        Assert.assertTrue(stringCollector.removeListener(str2));
        Assert.assertFalse(stringCollector.removeListener(str2));
        stringCollector.forEach(listener -> listener.writeToLog(log, (String s) -> s.toLowerCase(Locale.ENGLISH)));
        Assert.assertEquals(1, mockHandler.runnables.size());
        Assert.assertEquals("OneONETHREEonethree", log.toString());
        mockHandler.runnables.get(0).run();
        Assert.assertEquals("OneONETHREEonethreeTWO", log.toString());
        Assert.assertTrue(stringCollector.removeListener(str3));
        Assert.assertTrue(stringCollector.removeListener(str1));
        stringCollector.forEach(listener -> log.setLength(0));
        Assert.assertEquals("OneONETHREEonethreeTWO", log.toString());
    }

    @Test
    public void testConcurrentRemovalSingleThread() {
        final Collector<IMockListener> collector = new Collector<>(IMockListener.class);
        final MockListener mockListener = new MockListener();
        MockListener removingMockListener = new MockListener() {
            @Override
            public void onEvent(String event) {
                super.onEvent(event);
                collector.removeListener(mockListener);
            }
        };
        MockListener extraMockListener = new MockListener();
        collector.addListener(mockListener);
        collector.addListener(removingMockListener);
        collector.addListener(extraMockListener);

        Assert.assertEquals("", mockListener.getLog());
        collector.forEach(listener -> listener.onEvent("Test"));
        Assert.assertEquals("[Test]", mockListener.getLog());
        Assert.assertEquals("[Test]", removingMockListener.getLog());
        Assert.assertEquals("[Test]", extraMockListener.getLog());

        collector.forEach(listener -> listener.onEvent("UnitTest"));
        Assert.assertEquals("[Test]", mockListener.getLog());
        Assert.assertEquals("[Test][UnitTest]", removingMockListener.getLog());
        Assert.assertEquals("[Test][UnitTest]", extraMockListener.getLog());

        collector.removeListener(removingMockListener);
        collector.removeListener(extraMockListener);

        OpenContainer<Collection<?>> listenersFieldValue = ReflectionUtil.getDeclaredField(collector, OpenContainer.class, "listeners");
        Assert.assertEquals(0, listenersFieldValue.value.size());

        Map<?,?> wrapperForListenerField = ReflectionUtil.getDeclaredField(collector, Map.class, "wrapperForListener");
        Assert.assertEquals(0, wrapperForListenerField.size());

        Map<?,?> linkForListenerField = ReflectionUtil.getDeclaredField(collector, Map.class, "linkForListener");
        Assert.assertEquals(0, linkForListenerField.size());
    }

    @Test
    public void testConcurrentAdditionSingleThread() {
        final Collector<IMockListener> collector = new Collector<>(IMockListener.class);
        final MockListener lateMockListener = new MockListener();
        MockListener mockListener = new MockListener();
        MockListener addingMockListener = new MockListener() {
            @Override
            public void onEvent(String event) {
                super.onEvent(event);
                collector.addListener(lateMockListener);
            }
        };
        MockListener extraMockListener = new MockListener();
        collector.addListener(mockListener);
        collector.addListener(addingMockListener);
        collector.addListener(extraMockListener);

        Assert.assertEquals("", mockListener.getLog());
        collector.forEach(listener -> listener.onEvent("Test"));
        Assert.assertEquals("[Test]", mockListener.getLog());
        Assert.assertEquals("[Test]", addingMockListener.getLog());
        Assert.assertEquals("[Test]", extraMockListener.getLog());
        Assert.assertEquals("", lateMockListener.getLog());

        collector.forEach(listener -> listener.onEvent("UnitTest"));
        Assert.assertEquals("[Test][UnitTest]", mockListener.getLog());
        Assert.assertEquals("[Test][UnitTest]", addingMockListener.getLog());
        Assert.assertEquals("[Test][UnitTest]", extraMockListener.getLog());
        Assert.assertEquals("[UnitTest]", lateMockListener.getLog());

        Assert.assertTrue(collector.removeListener(mockListener));
        Assert.assertTrue(collector.removeListener(addingMockListener));
        Assert.assertTrue(collector.removeListener(extraMockListener));
        Assert.assertTrue(collector.removeListener(lateMockListener));

        OpenContainer<Collection<?>> listenersFieldValue = ReflectionUtil.getDeclaredField(collector, OpenContainer.class, "listeners");
        Assert.assertEquals(0, listenersFieldValue.value.size());

        Map<?,?> wrapperForListenerField = ReflectionUtil.getDeclaredField(collector, Map.class, "wrapperForListener");
        Assert.assertEquals(0, wrapperForListenerField.size());

        Map<?,?> linkForListenerField = ReflectionUtil.getDeclaredField(collector, Map.class, "linkForListener");
        Assert.assertEquals(0, linkForListenerField.size());
    }

    @Test
    public void testConcurrentRemovalMultipleThreads() throws InterruptedException {
        Interruptor interruptor = new Interruptor(Thread.currentThread(), 2000);
        interruptor.start();
        final Collector<IMockListener> collector = new Collector<>(IMockListener.class);
        MockListener mockListener1 = new SynchronizedMockListener();
        MockListener mockListener2 = new SynchronizedMockListener();
        MockListener mockListener3 = new SynchronizedMockListener();

        collector.addListener(mockListener1);
        collector.addListener(mockListener2);
        collector.addListener(mockListener3);

        final ThreadHalter mainTreadHalter = new ThreadHalter();
        final ThreadHalter secondThreadHalter = new ThreadHalter();
        new Thread(() -> {
            secondThreadHalter.waitForGoWrapException();
            collector.forEach(listener -> {
                listener.onEvent("Unit");
                secondThreadHalter.waitForGoWrapException();
            });
            mainTreadHalter.proceed();
            secondThreadHalter.waitForGoWrapException();
            collector.forEach(listener -> {
                listener.onEvent("Test");
                secondThreadHalter.waitForGoWrapException();
            });
            mainTreadHalter.proceed();
        }).start();

        secondThreadHalter.proceed(); //Start looping first

        secondThreadHalter.proceed();
        secondThreadHalter.proceed();
        secondThreadHalter.proceed();

        mainTreadHalter.waitForGo();
        Assert.assertEquals("[Unit]", mockListener1.getLog());
        Assert.assertEquals("[Unit]", mockListener2.getLog());
        Assert.assertEquals("[Unit]", mockListener3.getLog());

        secondThreadHalter.proceed(); //Start looping second


        secondThreadHalter.proceed();
        collector.removeListener(mockListener2); //Removal while iterating
        secondThreadHalter.proceed();
        secondThreadHalter.proceed();

        mainTreadHalter.waitForGo();
        Assert.assertEquals("[Unit][Test]", mockListener1.getLog());
        Assert.assertEquals("[Unit]", mockListener2.getLog());
        Assert.assertEquals("[Unit][Test]", mockListener3.getLog());

        interruptor.cancel();

        collector.forEach(listener -> collector.removeListener(listener));

        OpenContainer<Collection<?>> listenersFieldValue = ReflectionUtil.getDeclaredField(collector, OpenContainer.class, "listeners");
        Assert.assertEquals(0, listenersFieldValue.value.size());

        Map<?,?> wrapperForListenerField = ReflectionUtil.getDeclaredField(collector, Map.class, "wrapperForListener");
        Assert.assertEquals(0, wrapperForListenerField.size());

        Map<?,?> linkForListenerField = ReflectionUtil.getDeclaredField(collector, Map.class, "linkForListener");
        Assert.assertEquals(0, linkForListenerField.size());
    }

    private interface IStringTransform {
        String apply(String str);
    }
    private interface ILoggingMockListener extends IInternalListener {
        void writeToLog(StringBuilder log, IStringTransform stringTransform);
    }

    private static class LoggingMockListener implements ILoggingMockListener {
        private final String string;

        public LoggingMockListener(String string) {
            this.string = string;
        }

        @Override
        public void writeToLog(StringBuilder log, IStringTransform stringTransform) {
            log.append(stringTransform.apply(string));
        }
    }

    private interface IMockListener extends IInternalListener {
        void onEvent(String event);
    }

    private static class MockListener implements IMockListener {
        private final StringBuilder log = new StringBuilder();

        @Override
        public void onEvent(String event) {
            log.append("["+event+"]");
        }

        public String getLog() {
            return log.toString();
        }
    }

    private static class SynchronizedMockListener extends MockListener {
        @Override
        public synchronized void onEvent(String event) {
            super.onEvent(event);
        }

        public synchronized String getLog() {
            return super.getLog();
        }
    }
}
