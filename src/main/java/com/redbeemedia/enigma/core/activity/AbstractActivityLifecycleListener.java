// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.activity;

import android.os.Bundle;

public abstract class AbstractActivityLifecycleListener implements IActivityLifecycleListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onRestart() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
