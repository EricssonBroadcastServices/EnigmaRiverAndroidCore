package com.redbeemedia.enigma.core.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public class AndroidThreadUtil {
    private static IAndroidThreadHandler threadHandler = new DefaultThreadHandler();

    public static void runOnUiThread(Runnable runnable) {
        if(isOnUiThread()) {
            runnable.run();
        } else {
            threadHandler.postToUiThread(runnable);
        }
    }

    public static boolean isOnUiThread() {
        return threadHandler.isOnUiThread();
    }

    /**
     * For unit tests only.
     */
    /*package-protected*/ static void setAndroidThreadHandler(IAndroidThreadHandler threadHandler) {
        AndroidThreadUtil.threadHandler = threadHandler;
    }

    /**
     * For unit tests only.
     */
    /*package-protected*/ static IAndroidThreadHandler getThreadHandler() {
        return threadHandler;
    }

    /**
     * <p>Executes a {@code Callable<T>} from the ui/main thread, blocks while waiting for it to complete and finally returns the same value that the {@code Callable<T>} returned.</p>
     *
     * <p>If already on the ui/main thread, this method just calls <pre>callable.call()</pre>.</p>
     *
     * <p>If the {@code Callable<T>} throw an exception it is wrapped in a {@code RuntimeException} and throw on the calling thread.</p>
     *
     * <p>If the {@code Callable<T>} has not completed before {@code timeoutMillis} the main thread is interrupted and this method throws a {@code TimeoutException}.</p>
     *
     * @param timeoutMillis Maximum amount of milliseconds to block before throwing a {@code TimeoutException}.
     * @param callable The {@code Callable<T>} to be called on the ui/main thread.
     * @param <T> return type
     * @return {@code callable.call()}
     * @throws InterruptedException If the calling thread is interrupted while waiting for the result.
     * @throws TimeoutException If no result has been returned before {@code timeoutMillis} milliseconds.
     */
    public static <T> T getBlockingOnUiThread(long timeoutMillis, Callable<T> callable) throws InterruptedException, TimeoutException {
        if(isOnUiThread()) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            final DelayedResult<T> delayedResult = new DelayedResult<>();
            runOnUiThread(() -> {
                delayedResult.bindToThread();
                try {
                    delayedResult.setResult(callable.call());
                } catch (Exception e) {
                    delayedResult.setException(e);
                }
            });
            return delayedResult.getBlocking(timeoutMillis);
        }
    }

    /**
     * <p>Executes a {@code Callable<T>} from the ui/main thread, blocks while waiting for it to complete and finally returns the same value that the {@code Callable<T>} returned.</p>
     * <p>This method blocks until the {@code callable.call()} returns or throws an exception.</p>
     * <p>{@code getBlockingOnUiThread(callable)} is equivalent to calling {@code getBlockingOnUiThread(0, callable)}.</p>
     * @param callable The {@code Callable<T>} to be called on the ui/main thread.
     * @param <T> return type
     * @return {@code callable.call()}
     * @throws InterruptedException
     * @see #getBlockingOnUiThread(long, Callable)
     */
    public static <T> T getBlockingOnUiThread(Callable<T> callable) throws InterruptedException {
        try {
            return getBlockingOnUiThread(0, callable);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies that {@link AndroidThreadUtil#isOnUiThread} is true for the current thread. If not, an {@link IllegalStateException} is thrown.
     */
    public static void verifyCalledFromUiThread() throws IllegalStateException {
        if(!isOnUiThread()) {
            throw new IllegalStateException("Must be called from the main thread.");
        }
    }

    private static class DelayedResult<T> {
        private final OpenContainer<IResult<T>> resultContainer = new OpenContainer<>(null);
        private volatile Thread boundThread = null;

        public T getBlocking(long timeoutMillis) throws InterruptedException, TimeoutException {
            IResult<T> result;
            long startMillis = System.nanoTime()/1000000L;
            while((result = OpenContainerUtil.getValueSynchronized(resultContainer)) == null) {
                long nowMillis = System.nanoTime()/1000000L;
                if(timeoutMillis > 0 && nowMillis - startMillis > timeoutMillis) {
                    TimeoutException timeoutException =  new TimeoutException();
                    try {
                        if(boundThread != null) {
                            boundThread.interrupt();
                        } else {
                            throw new InterruptedException();
                        }
                    } catch (Exception e) {
                        timeoutException.addSuppressed(e);
                    }
                    throw timeoutException;
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }
            return result.get();
        }

        private void bindToThread() {
            this.boundThread = Thread.currentThread();
        }

        public void setResult(final T result) {
            OpenContainerUtil.setValueSynchronized(resultContainer, new IResult<T>() {
                @Override
                public T get() {
                    return result;
                }
            }, null);
        }

        public void setException(final Exception e) {
            OpenContainerUtil.setValueSynchronized(resultContainer, new IResult<T>() {
                @Override
                public T get() {
                    throw new RuntimeException(e);
                }
            }, null);
        }

        private interface IResult<T> {
            T get();
        }
    }

    /*package-protected*/ interface IAndroidThreadHandler {
        boolean isOnUiThread();
        void postToUiThread(Runnable runnable);
    }

    private static class DefaultThreadHandler implements IAndroidThreadHandler {
        private static Handler mainHandler = new Handler(Looper.getMainLooper());

        @Override
        public boolean isOnUiThread() {
            return Looper.getMainLooper() == Looper.myLooper();
        }

        @Override
        public void postToUiThread(Runnable runnable) {
            mainHandler.post(runnable);
        }
    }
}
