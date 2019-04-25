package com.redbeemedia.enigma.core.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class ProxyCallback {

    public static <T extends IInternalCallbackObject> T createCallbackOnThread(IHandler handler, Class<T> callbackInterface, T callback) {
        return wrapInProxy(handler, callbackInterface, callback);
    }

    public static <T extends IInternalListener> T createListenerWithHandler(IHandler handler, Class<T> listenerInterface, T listener) {
        return wrapInProxy(handler, listenerInterface, listener);
    }

    private static <T> T wrapInProxy(IHandler handler, Class<T> proxyInterface, T wrapped) {
        return (T) Proxy.newProxyInstance(proxyInterface.getClassLoader(), new Class[]{proxyInterface}, new HandlerInvocationHandler(handler, wrapped));
    }

    private static class HandlerInvocationHandler implements InvocationHandler {
        private IHandler handler;
        private Object originalObject;

        public HandlerInvocationHandler(IHandler handler, Object originalObject) {
            if(handler == null) {
                throw new NullPointerException();
            }
            this.handler = handler;
            this.originalObject = originalObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(method.getDeclaringClass().equals(Object.class) && "equals".equals(method.getName())) {
                return proxy == args[0];
            } else {
                StackTraceElement[] callTrace = Thread.currentThread().getStackTrace();
                handler.post(new RunnableMethodInvocation(callTrace, method, originalObject, args));
                return null;
            }
        }
    }

    private static class RunnableMethodInvocation implements Runnable {
        private final Method method;
        private final Object object;
        private final Object[] args;

        public RunnableMethodInvocation(Method method, Object object, Object[] args) {
                this.method = method;
                this.object = object;
                this.args = args;
        }

        @Override
        public void run() {
            try {
                method.invoke(object, args);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e); //TODO handle better
            } catch (InvocationTargetException e) {
                doThrow(e.getTargetException());
            }
        }

        private <T extends Throwable> void doThrow(Throwable t) throws T {
            throw (T) t;
        }
    }
}
