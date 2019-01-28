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
