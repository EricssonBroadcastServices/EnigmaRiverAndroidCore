// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.activity;

import android.os.Bundle;

public interface IActivityLifecycleListener {
    void onCreate(Bundle savedInstanceState);
    void onStart();
    void onResume();
    void onPause();
    void onStop();
    void onDestroy();
    void onRestart();
    void onSaveInstanceState(Bundle outState);
}
