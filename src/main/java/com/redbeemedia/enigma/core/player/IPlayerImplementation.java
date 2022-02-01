package com.redbeemedia.enigma.core.player;

import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public interface IPlayerImplementation {
    void install(IEnigmaPlayerEnvironment environment);
    void release();
    void setupPlayerNotificationManager(PlayerNotificationManager manager);
    void updateTimeBar(long millis);
}
