package com.redbeemedia.enigma.core.testutil;

import org.junit.Assert;

public class Counter {
    private int counts = 0;

    public void count() {
        counts++;
    }

    public int getCounts() {
        return counts;
    }

    public void assertNone() {
        Assert.assertEquals(0, counts);
    }

    public void assertOnce() {
        Assert.assertEquals(1, counts);
    }

    public void assertOneOrMore() {
        Assert.assertTrue( counts > 0);
    }

    public void assertCount(int times) {
        Assert.assertEquals(times, counts);
    }
}
