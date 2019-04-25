package com.redbeemedia.enigma.core.util;

import java.util.ArrayList;
import java.util.List;

public class MockHandler implements IHandler {
    public final List<Runnable> runnables = new ArrayList<>();

    @Override
    public boolean post(Runnable runnable) {
        runnables.add(runnable);
        return true;
    }

    @Override
    public boolean postDelayed(Runnable runnable, long delayMillis) {
        runnables.add(runnable);
        return true;
    }
}