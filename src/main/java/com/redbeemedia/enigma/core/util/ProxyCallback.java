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

    public static <T extends IInternalCallbackObject> T useCallbackHandlerIfPresent(IHandler callbackHandler, Class<T> callbackInterface, T callback) {
        if(callbackHandler != null) {
            return ProxyCallback.createCallbackOnThread(callbackHandler, callbackInterface, callback);
        } else {
            return callback;
        }
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
            } else if("toString".equals(method.getName())) {
                return "Proxy";
            } else {
                StackTraceElement[] callTrace = Thread.currentThread().getStackTrace();
                handler.post(new RunnableMethodInvocation(callTrace, method, originalObject, args));
                return null;
            }
        }
    }

    private static class RunnableMethodInvocation implements Runnable {
        private static final StackTraceElement[] MAGIC_STE = new StackTraceElement[]{new StackTraceElement("vitrual","magicThreadSwitching","magic",0)};

        private final StackTraceElement[] callStack;
        private final Method method;
        private final Object object;
        private final Object[] args;

        public RunnableMethodInvocation(StackTraceElement[] callStack, Method method, Object object, Object[] args) {
                this.callStack = callStack;
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
                Throwable throwable = e.getTargetException();
                StackTraceElement[] throwableStack = throwable.getStackTrace();
                //Remove all before and including the "run-method call"
                int cutoffPoint = throwableStack.length;
                for(int i = 0; i < throwableStack.length; ++i) {
                    if(this.getClass().getName().equals(throwableStack[i].getClassName()) && "run".equals(throwableStack[i].getMethodName())) {
                        cutoffPoint = i;
                        break;
                    }
                }
                StackTraceElement[] merged = new StackTraceElement[cutoffPoint+callStack.length-5+1]; //First 6 from the callStack are removed
                System.arraycopy(throwableStack, 0, merged, 0, cutoffPoint);
                System.arraycopy(MAGIC_STE,0, merged, cutoffPoint, 1);
                System.arraycopy(callStack, 5, merged, cutoffPoint+1, callStack.length-5);
                throwable.setStackTrace(merged);
                doThrow(throwable);
            }
        }

        private <T extends Throwable> void doThrow(Throwable t) throws T {
            throw (T) t;
        }
    }
}
