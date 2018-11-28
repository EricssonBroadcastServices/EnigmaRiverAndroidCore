package com.redbeemedia.enigma.core.testutil;

public class Flag {
    private boolean set = false;

    public boolean isTrue() {
        return set;
    }

    public void setFlag() {
        this.set = true;
    }
}
