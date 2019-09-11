package com.redbeemedia.enigma.core.util;

import java.util.Objects;

public class OpenContainerUtil {

    public static <T> void setValueSynchronized(OpenContainer<T> container, T newValue, IValueChangedListener<T> valueChangedListener) {
        T oldValue;
        synchronized (container) {
            oldValue = container.value;
            container.value = newValue;
        }
        if(!Objects.equals(oldValue, newValue)) {
            valueChangedListener.onValueChanged(oldValue, newValue);
        }
    }

    public static <T> T getValueSynchronized(OpenContainer<T> container) {
        synchronized (container) {
            return container.value;
        }
    }
}
