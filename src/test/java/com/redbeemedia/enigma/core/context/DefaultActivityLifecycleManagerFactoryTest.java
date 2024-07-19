// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.context;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.redbeemedia.enigma.core.activity.AbstractActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.testutil.MockApplication;

import org.junit.Test;

public class DefaultActivityLifecycleManagerFactoryTest {

    private static IActivityLifecycleManager getActivityLifecycleManager(final Application application) {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setActivityLifecycleManagerFactory(new DefaultActivityLifecycleManagerFactory() {
            @Override
            public IActivityLifecycleManager createActivityLifecycleManager(Application possiblyNull) {
                return super.createActivityLifecycleManager(possiblyNull == null ? application : possiblyNull);
            }
        }));
        DefaultActivityLifecycleManagerFactory managerFactory = new DefaultActivityLifecycleManagerFactory();
        return managerFactory.createActivityLifecycleManager(application);
    }

    @Test
    public void testOnStart() {
        MockApplication application = new MockApplication();
        IActivityLifecycleManager activityLifecycleManager = getActivityLifecycleManager(application);
        Activity activity1 = new Activity();
        Activity activity2 = new Activity();
        final Flag onActivity1Start = new Flag();
        final Flag onActivity2Start = new Flag();
        activityLifecycleManager.add(activity1, new AbstractActivityLifecycleListener() {
            @Override
            public void onStart() {
                onActivity1Start.setFlag();
            }
        });
        activityLifecycleManager.add(activity2, new AbstractActivityLifecycleListener() {
            @Override
            public void onStart() {
                onActivity2Start.setFlag();
            }
        });

        onActivity1Start.assertNotSet();
        onActivity2Start.assertNotSet();

        application.fireActivityLifecycleEvent(activity1, MockApplication.ActivityLifecycleEvent.STARTED);

        onActivity1Start.assertSet();
        onActivity2Start.assertNotSet();

        application.fireActivityLifecycleEvent(activity2, MockApplication.ActivityLifecycleEvent.CREATED);

        onActivity2Start.assertNotSet();

        application.fireActivityLifecycleEvent(activity2, MockApplication.ActivityLifecycleEvent.STARTED);

        onActivity2Start.assertSet();
    }

    /**
     * Template method for testing all the different events.
     *
     */
    private void testEventPropagated(IActivityLifecycleListener activityLifecycleListener, Flag onEvent, MockApplication.ActivityLifecycleEvent event) {
        MockApplication application = new MockApplication();
        IActivityLifecycleManager activityLifecycleManager = getActivityLifecycleManager(application);
        Activity activity = new Activity();
        activityLifecycleManager.add(activity, activityLifecycleListener);

        onEvent.assertNotSet();

        application.fireActivityLifecycleEvent(activity, event);

        onEvent.assertSet();
    }

    @Test
    public void testOnResume() {
        final Flag onEvent = new Flag();
        testEventPropagated(new AbstractActivityLifecycleListener() {
            @Override
            public void onResume() {
                onEvent.setFlag();
            }
        }, onEvent, MockApplication.ActivityLifecycleEvent.RESUMED);
    }

    @Test
    public void testOnCreate() {
        final Flag onEvent = new Flag();
        testEventPropagated(new AbstractActivityLifecycleListener() {
            @Override
            public void onCreate(Bundle savedInstanceState) {
                onEvent.setFlag();
            }
        }, onEvent, MockApplication.ActivityLifecycleEvent.CREATED);
    }

    @Test
    public void testOnPause() {
        final Flag onEvent = new Flag();
        testEventPropagated(new AbstractActivityLifecycleListener() {
            @Override
            public void onPause() {
                onEvent.setFlag();
            }
        }, onEvent, MockApplication.ActivityLifecycleEvent.PAUSED);
    }

    @Test
    public void testOnDestroy() {
        final Flag onEvent = new Flag();
        testEventPropagated(new AbstractActivityLifecycleListener() {
            @Override
            public void onDestroy() {
                onEvent.setFlag();
            }
        }, onEvent, MockApplication.ActivityLifecycleEvent.DESTROYED);
    }

    @Test
    public void testOnSaveInstanceState() {
        final Flag onEvent = new Flag();
        testEventPropagated(new AbstractActivityLifecycleListener() {
            @Override
            public void onSaveInstanceState(Bundle outState) {
                onEvent.setFlag();
            }
        }, onEvent, MockApplication.ActivityLifecycleEvent.SAVE_INSTANCE_STATE);
    }

    @Test
    public void testOnStop() {
        final Flag onEvent = new Flag();
        testEventPropagated(new AbstractActivityLifecycleListener() {
            @Override
            public void onStop() {
                onEvent.setFlag();
            }
        }, onEvent, MockApplication.ActivityLifecycleEvent.STOPPED);
    }
}
