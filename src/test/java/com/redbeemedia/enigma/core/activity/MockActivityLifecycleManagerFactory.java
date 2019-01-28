package com.redbeemedia.enigma.core.activity;

import android.app.Activity;
import android.app.Application;

public class MockActivityLifecycleManagerFactory implements IActivityLifecycleManagerFactory {
    @Override
    public IActivityLifecycleManager createActivityLifecycleManager(Application application) {
        return new IActivityLifecycleManager() {
            @Override
            public void remove(Activity activity, IActivityLifecycleListener activityLifecycleListener) {
            }

            @Override
            public void add(Activity activity, IActivityLifecycleListener activityLifecycleListener) {
            }
        };
    }
}
