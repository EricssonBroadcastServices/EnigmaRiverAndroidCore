package com.redbeemedia.enigma.core.util;

public interface IValueChangedListener<T> {
    void onValueChanged(T oldValue, T newValue);
}
