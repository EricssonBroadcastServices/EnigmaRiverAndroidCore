package com.redbeemedia.enigma.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class CollectorTest {
    @Test
    public void testCollector() {
        StringBuilder log = new StringBuilder();
        Collector<String> stringCollector = new Collector<>();
        String str1 = "One";
        String str2 = "Two";
        String str3 = "Three";
        Assert.assertTrue(stringCollector.addListener(str1));
        stringCollector.forEach(listener -> log.append(listener));
        Assert.assertEquals("One", log.toString());
        Assert.assertTrue(stringCollector.addListener(str2));
        Assert.assertTrue(stringCollector.addListener(str3));
        stringCollector.forEach(listener -> log.append(listener.toUpperCase(Locale.ENGLISH)));
        Assert.assertEquals("OneONETWOTHREE", log.toString());
        Assert.assertTrue(stringCollector.removeListener(str2));
        Assert.assertFalse(stringCollector.removeListener(str2));
        stringCollector.forEach(listener -> log.append(listener.toLowerCase(Locale.ENGLISH)));
        Assert.assertEquals("OneONETWOTHREEonethree", log.toString());
        Assert.assertTrue(stringCollector.removeListener(str3));
        Assert.assertTrue(stringCollector.removeListener(str1));
        stringCollector.forEach(listener -> log.setLength(0));
        Assert.assertEquals("OneONETWOTHREEonethree", log.toString());
    }
}
