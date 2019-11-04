package com.redbeemedia.enigma.core.util;

import android.os.Handler;
import android.os.Looper;

public class AndroidThreadUtil {
    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(Runnable runnable) {
        if(isOnUiThread()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    public static boolean isOnUiThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * Verifies that {@link AndroidThreadUtil#isOnUiThread} is true for the current thread. If not, an {@link IllegalStateException} is thrown.
     */
    public static void verifyCalledFromUiThread() throws IllegalStateException {
        if(!isOnUiThread()) {
            throw new IllegalStateException("Must be called from the main thread.");
        }
    }
}
