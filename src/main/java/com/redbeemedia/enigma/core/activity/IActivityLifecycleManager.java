// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.activity;

import android.app.Activity;

public interface IActivityLifecycleManager {
    void remove(Activity activity, IActivityLifecycleListener activityLifecycleListener);
    void add(Activity activity, IActivityLifecycleListener activityLifecycleListener);
}
