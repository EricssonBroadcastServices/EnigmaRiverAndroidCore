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
    private final Class<T> listenerType;
    private final OpenContainer<Collection<T>> listeners = new OpenContainer<>(new ArrayList<>());
    private Map<T,T> wrapperForListener = new HashMap<>();

    public Collector(Class<T> listenerType) {
        this.listenerType = listenerType;
    }

    public boolean addListener(T listener) {
        if(listener == null) {
            throw new NullPointerException();
        }
        synchronized (listeners) {
            return listeners.value.add(listener);
        }
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
            if(wrapper == null) {
                throw new RuntimeException("Failed to create proxy!");
            }
            wrapperForListener.put(listener, wrapper);
            synchronized (listeners) {
                return listeners.value.add(wrapper);
            }
        }
    }

    public boolean removeListener(T listener) {
        T wrapper = wrapperForListener.get(listener);
        if(wrapper != null) {
            wrapperForListener.remove(listener);
            synchronized (listeners) {
                return listeners.value.remove(wrapper);
            }
        } else {
            synchronized (listener) {
                return listeners.value.remove(listener);
            }
        }
    }

    protected void forEach(IListenerAction<T> listenerAction) {
        synchronized (listeners) {
            for(T listener : listeners.value) {
                listenerAction.onListener(listener);
            }
        }
    }

    protected interface IListenerAction<T> {
        void onListener(T listener);
    }


}
