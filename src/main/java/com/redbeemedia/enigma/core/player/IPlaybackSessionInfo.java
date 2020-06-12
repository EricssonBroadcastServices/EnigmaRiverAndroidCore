package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.video.IVideoTrack;

/*package-protected*/ interface IPlaybackSessionInfo {
    Duration getCurrentPlaybackOffset();
    String getAssetId();
    IPlayable getPlayable();
    String getMediaLocator();
    String getPlayerTechnologyName();
    String getPlayerTechnologyVersion();
    String getCurrentProgramId();
}
