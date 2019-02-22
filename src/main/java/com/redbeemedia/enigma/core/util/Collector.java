package com.redbeemedia.enigma.core.util;

import java.util.ArrayList;
import java.util.Collection;

public class Collector<T> {
    private Collection<T> listeners = new ArrayList<>();

    public boolean addListener(T listener) {
        if(listener == null) {
            throw new NullPointerException();
        }
        return listeners.add(listener);
    }

    public boolean removeListener(T listener) {
        return listeners.remove(listener);
    }

    protected void forEach(IListenerAction<T> listenerAction) {
        for(T listener : listeners) {
            listenerAction.onListener(listener);
        }
    }

    protected interface IListenerAction<T> {
        void onListener(T listener);
    }
}
