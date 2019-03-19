package com.redbeemedia.enigma.core.activity;

import android.app.Application;

public interface IActivityLifecycleManagerFactory {
    IActivityLifecycleManager createActivityLifecycleManager(Application application);
}
