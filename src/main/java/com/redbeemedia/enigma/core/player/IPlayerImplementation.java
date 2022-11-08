package com.redbeemedia.enigma.core.player;

import android.content.Context;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.SubtitleView;

public interface IPlayerImplementation {
    void install(IEnigmaPlayerEnvironment environment);
    void release();
    void setupPlayerNotificationManager(PlayerNotificationManager manager);
    MediaSessionCompat createMediaSession(Context applicationContext);
    void updateTimeBar(long millis);
    Player getInternalPlayer();
    SubtitleView getPlayerSubtitleView();
}
