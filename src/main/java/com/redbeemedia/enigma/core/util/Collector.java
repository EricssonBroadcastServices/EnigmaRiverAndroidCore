package com.redbeemedia.enigma.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class Collector<T extends IInternalListener> {
    private Class<T> listenerType;
    private Collection<T> listeners = new ArrayList<>();
    private Map<T,T> wrapperForListener = new HashMap<>();

    public Collector(Class<T> listenerType) {
        this.listenerType = listenerType;
    }

    public boolean addListener(T listener) {
        if(listener == null) {
            throw new NullPointerException();
        }
        return listeners.add(listener);
    }

    public boolean addListener(T listener, IHandler handler) {
        if(listener == null) {
            throw new NullPointerException();
        }
        if(handler == null) {
            throw new NullPointerException();
        }
        T wrapper = wrapperForListener.get(listener);
        if(wrapper != null) {
            return false;
        } else {
            wrapper = ProxyCallback.createListenerWithHandler(handler, listenerType, listener);
            wrapperForListener.put(listener, wrapper);
            return listeners.add(wrapper);
        }
    }

    public boolean removeListener(T listener) {
        T wrapper = wrapperForListener.get(listener);
        if(wrapper != null) {
            wrapperForListener.remove(listener);
            return listeners.remove(wrapper);
        } else {
            return listeners.remove(listener);
        }
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
