package com.redbeemedia.enigma.core.testutil;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;

public class MockApplication extends Application {
    private Collection<ActivityLifecycleCallbacks> activityLifecycleCallbacks = new ArrayList<>();

    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callbacks) {
        activityLifecycleCallbacks.add(callbacks);
    }

    public void fireActivityLifecycleEvent(Activity activity, ActivityLifecycleEvent event) {
        for(ActivityLifecycleCallbacks activityLifecycleCallback : activityLifecycleCallbacks) {
            event.fire(activityLifecycleCallback, activity);
        }
    }

    public enum ActivityLifecycleEvent {
        CREATED {
            @Override
            void fire(ActivityLifecycleCallbacks activityLifecycleCallback, Activity activity) {
                activityLifecycleCallback.onActivityCreated(activity, new Bundle());
            }
        },
        STARTED {
            @Override
            void fire(Application.ActivityLifecycleCallbacks activityLifecycleCallback, Activity activity) {
                activityLifecycleCallback.onActivityStarted(activity);
            }
        },
        RESUMED {
            @Override
            void fire(Application.ActivityLifecycleCallbacks activityLifecycleCallback, Activity activity) {
                activityLifecycleCallback.onActivityResumed(activity);
            }
        },
        PAUSED {
            @Override
            void fire(Application.ActivityLifecycleCallbacks activityLifecycleCallback, Activity activity) {
                activityLifecycleCallback.onActivityPaused(activity);
            }
        },
        STOPPED {
            @Override
            void fire(Application.ActivityLifecycleCallbacks activityLifecycleCallback, Activity activity) {
                activityLifecycleCallback.onActivityStopped(activity);
            }
        },
        SAVE_INSTANCE_STATE {
            @Override
            void fire(Application.ActivityLifecycleCallbacks activityLifecycleCallback, Activity activity) {
                activityLifecycleCallback.onActivitySaveInstanceState(activity, new Bundle());
            }
        }, DESTROYED {
            @Override
            void fire(Application.ActivityLifecycleCallbacks activityLifecycleCallback, Activity activity) {
                activityLifecycleCallback.onActivityDestroyed(activity);
            }
        };


        abstract void fire(ActivityLifecycleCallbacks activityLifecycleCallback, Activity activity);
    }
}
