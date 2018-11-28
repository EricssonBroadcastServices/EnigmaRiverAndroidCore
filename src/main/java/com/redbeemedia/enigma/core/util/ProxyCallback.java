package com.redbeemedia.enigma.core.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyCallback {

    public static <T> T createCallbackOnThread(IHandler handler, Class<T> callbackInterface, T callback) {
        return (T) Proxy.newProxyInstance(callbackInterface.getClassLoader(), new Class[]{callbackInterface}, new HandlerInvocationHandler(handler, callback));
    }

    private static class HandlerInvocationHandler implements InvocationHandler {
        private WeakReference<IHandler> handlerReference;
        private Object originalObject;

        public HandlerInvocationHandler(IHandler handler, Object originalObject) {
            this.handlerReference = new WeakReference<>(handler);
            this.originalObject = originalObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            IHandler handler = handlerReference.get();
            if(handler != null) {
                handler.post(new RunnableMethodInvocation(method, originalObject, args));
            } else {
                //Release reference to original object
                originalObject = null;
            }
            return null;
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
                throw new RuntimeException(e);//TODO handle better
            }
        }
    }
}
