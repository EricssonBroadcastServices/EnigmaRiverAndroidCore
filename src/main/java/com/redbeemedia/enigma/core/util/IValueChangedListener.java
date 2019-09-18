package com.redbeemedia.enigma.core.util;

public interface IValueChangedListener<T> extends IInternalListener {
    void onValueChanged(T oldValue, T newValue);
}
