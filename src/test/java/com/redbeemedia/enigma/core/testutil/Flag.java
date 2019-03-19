package com.redbeemedia.enigma.core.testutil;

import org.junit.Assert;

public class Flag {
    private boolean set = false;

    public boolean isTrue() {
        return set;
    }

    public void setFlag() {
        this.set = true;
    }

    public void assertSet() {
        Assert.assertTrue(set);
    }

    public void assertSet(String message) {
        Assert.assertTrue(message, set);
    }

    public void assertNotSet() {
        Assert.assertFalse(set);
    }

    public void assertNotSet(String message) {
        Assert.assertFalse(message, set);
    }
}
