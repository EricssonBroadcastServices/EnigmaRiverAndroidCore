package com.redbeemedia.enigma.core.util;

public class OpenContainer<T> {
    public volatile T value;

    public OpenContainer(T value) {
        this.value = value;
    }
}
