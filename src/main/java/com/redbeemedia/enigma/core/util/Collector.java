package com.redbeemedia.enigma.core.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class Collector<T extends IInternalListener> {
    private final Class<T> listenerType;
    private final OpenContainer<Collection<ListenerLink<T>>> listeners = new OpenContainer<>(new CopyOnWriteArrayList<>());
    private Map<T,T> wrapperForListener = new HashMap<>();
    private Map<T,ListenerLink<T>> linkForListener = new HashMap<>();

    public Collector(Class<T> listenerType) {
        this.listenerType = listenerType;
    }

    public boolean addListener(T listener) {
        if(listener == null) {
            throw new NullPointerException();
        }
        synchronized (listeners) {
            ListenerLink<T> existingLink = linkForListener.get(listener);
            if(existingLink != null && listeners.value.contains(existingLink)) {
                return false;
            } else {
                ListenerLink<T> link = new ListenerLink<>(listener);
                linkForListener.put(listener, link);
                return listeners.value.add(link);
            }
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
                ListenerLink<T> link = new ListenerLink<>(wrapper);
                linkForListener.put(listener, link);
                return listeners.value.add(link);
            }
        }
    }

    public boolean removeListener(T listener) {
        T wrapper = wrapperForListener.get(listener);
        if(wrapper != null) {
            wrapperForListener.remove(listener);
        }
        return removeLink(listener);
    }

    private boolean removeLink(T listener) {
        synchronized (listeners) {
            ListenerLink<T> listenerLink = linkForListener.remove(listener);
            if(listenerLink != null && !listenerLink.isStale()) {
                listenerLink.remove();
                return listeners.value.remove(listenerLink);
            } else {
                return false;
            }
        }
    }

    protected void forEach(IListenerAction<T> listenerAction) {
        RuntimeException exception = null;
        for(ListenerLink<T> listenerLink : OpenContainerUtil.getValueSynchronized(listeners)) {
            try {
                listenerLink.execute(listenerAction);
            } catch (RuntimeException e) {
                if(exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }

        //Clear stale links
        synchronized (listeners) {
            Iterator<ListenerLink<T>> iterator = listeners.value.iterator();
            while(iterator.hasNext()) {
                ListenerLink<T> link = iterator.next();
                if(link.isStale()) {
                    iterator.remove();
                }
            }
        }
        if(exception != null) {
            throw exception;
        }
    }

    protected interface IListenerAction<T> {
        void onListener(T listener);
    }

    private static class ListenerLink<T> {
        private T link;

        public ListenerLink(T link) {
            this.link = link;
        }

        public synchronized void execute(IListenerAction<T> action) {
            if(link != null) {
                action.onListener(link);
            }
        }

        public synchronized void remove() {
            link = null;
        }

        public synchronized boolean isStale() {
            return link == null;
        }
    }
}
