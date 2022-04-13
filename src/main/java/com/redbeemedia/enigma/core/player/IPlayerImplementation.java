package com.redbeemedia.enigma.core.player;

import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.SubtitleView;

public interface IPlayerImplementation {
    void install(IEnigmaPlayerEnvironment environment);
    void release();
    void setupPlayerNotificationManager(PlayerNotificationManager manager);
    void updateTimeBar(long millis);
    SubtitleView getPlayerSubtitleView();
}
