package com.redbeemedia.enigma.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class CollectorTest {
    @Test
    public void testForEach() {
        StringBuilder log = new StringBuilder();
        Collector<IMockListener> stringCollector = new Collector<>(IMockListener.class);
        MockListener str1 = new MockListener("One");
        MockListener str2 = new MockListener("Two");
        MockListener str3 = new MockListener("Three");
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
    public void testCollectorWithHandlers() {
        StringBuilder log = new StringBuilder();
        Collector<IMockListener> stringCollector = new Collector<>(IMockListener.class);
        MockListener str1 = new MockListener("One");
        MockListener str2 = new MockListener("Two");
        MockListener str3 = new MockListener("Three");
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

    private interface IStringTransform {
        String apply(String str);
    }
    private interface IMockListener extends IInternalListener {
        void writeToLog(StringBuilder log, IStringTransform stringTransform);
    }

    private static class MockListener implements IMockListener {
        private final String string;

        public MockListener(String string) {
            this.string = string;
        }

        @Override
        public void writeToLog(StringBuilder log, IStringTransform stringTransform) {
            log.append(stringTransform.apply(string));
        }
    }
}
