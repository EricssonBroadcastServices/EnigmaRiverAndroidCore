package com.redbeemedia.enigma.core.context;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.redbeemedia.enigma.core.activity.IActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManagerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

/*package-protected*/ class DefaultActivityLifecycleManagerFactory implements IActivityLifecycleManagerFactory {
    @Override
    public IActivityLifecycleManager createActivityLifecycleManager(Application application) {
        ActivityLifecycleManager activityLifecycleManager = new ActivityLifecycleManager();
        application.registerActivityLifecycleCallbacks(activityLifecycleManager);
        return activityLifecycleManager;
    };

    private static class ActivityLifecycleEventBroadcaster implements IActivityLifecycleListener {
        private final Collection<IActivityLifecycleListener> listeners = new ArrayList<>();
        private final Collection<IActivityLifecycleListener> snapshotListeners = new ArrayList<>();//Used for iteration. So that we can edit listeners while iterating.

        public void addListener(IActivityLifecycleListener listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }

        public void removeListener(IActivityLifecycleListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            synchronized (snapshotListeners) {
                snapshotListeners.clear();
                synchronized (listeners) {
                    snapshotListeners.addAll(listeners);
                }
                for(IActivityLifecycleListener listener : snapshotListeners) {
                    listener.onCreate(savedInstanceState);
                }
            }
        }

        @Override
        public void onStart() {
            synchronized (snapshotListeners) {
                snapshotListeners.clear();
                synchronized (listeners) {
                    snapshotListeners.addAll(listeners);
                }
                for (IActivityLifecycleListener listener : snapshotListeners) {
                    listener.onStart();
                }
            }
        }

        @Override
        public void onResume() {
            synchronized (snapshotListeners) {
                snapshotListeners.clear();
                synchronized (listeners) {
                    snapshotListeners.addAll(listeners);
                }
                for (IActivityLifecycleListener listener : snapshotListeners) {
                    listener.onResume();
                }
            }
        }

        @Override
        public void onPause() {
            synchronized (snapshotListeners) {
                snapshotListeners.clear();
                synchronized (listeners) {
                    snapshotListeners.addAll(listeners);
                }
                for (IActivityLifecycleListener listener : snapshotListeners) {
                    listener.onPause();
                }
            }
        }

        @Override
        public void onStop() {
            synchronized (snapshotListeners) {
                snapshotListeners.clear();
                synchronized (listeners) {
                    snapshotListeners.addAll(listeners);
                }
                for (IActivityLifecycleListener listener : snapshotListeners) {
                    listener.onStop();
                }
            }
        }

        @Override
        public void onDestroy() {
            synchronized (snapshotListeners) {
                snapshotListeners.clear();
                synchronized (listeners) {
                    snapshotListeners.addAll(listeners);
                }
                for (IActivityLifecycleListener listener : snapshotListeners) {
                    listener.onDestroy();
                }
            }
        }

        @Override
        public void onRestart() {
            synchronized (snapshotListeners) {
                snapshotListeners.clear();
                synchronized (listeners) {
                    snapshotListeners.addAll(listeners);
                }
                for (IActivityLifecycleListener listener : snapshotListeners) {
                    listener.onRestart();
                }
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            synchronized (snapshotListeners) {
                snapshotListeners.clear();
                synchronized (listeners) {
                    snapshotListeners.addAll(listeners);
                }
                for (IActivityLifecycleListener listener : snapshotListeners) {
                    listener.onSaveInstanceState(outState);
                }
            }
        }
    }

    private static class ActivityLifecycleManager implements IActivityLifecycleManager, Application.ActivityLifecycleCallbacks {
        private final Map<Activity, ActivityLifecycleEventBroadcaster> eventBroadcasters = new WeakHashMap<>();

        private ActivityLifecycleEventBroadcaster getBroadcaster(Activity activity) {
            synchronized (eventBroadcasters) {
                ActivityLifecycleEventBroadcaster eventBroadcaster = eventBroadcasters.get(activity);
                if (eventBroadcaster == null) {
                    eventBroadcaster = new ActivityLifecycleEventBroadcaster();
                    eventBroadcasters.put(activity, eventBroadcaster);
                }
                return eventBroadcaster;
            }
        }

        @Override
        public void remove(Activity activity, IActivityLifecycleListener activityLifecycleListener) {
            getBroadcaster(activity).removeListener(activityLifecycleListener);
        }

        @Override
        public void add(Activity activity, IActivityLifecycleListener activityLifecycleListener) {
            getBroadcaster(activity).addListener(activityLifecycleListener);
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            getBroadcaster(activity).onCreate(savedInstanceState);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            getBroadcaster(activity).onStart();
        }

        @Override
        public void onActivityResumed(Activity activity) {
            getBroadcaster(activity).onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            getBroadcaster(activity).onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {
            getBroadcaster(activity).onStop();
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            getBroadcaster(activity).onSaveInstanceState(outState);
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            getBroadcaster(activity).onDestroy();
        }
    }
}
