// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.activity;

import android.app.Application;

public interface IActivityLifecycleManagerFactory {
    IActivityLifecycleManager createActivityLifecycleManager(Application application);
}
