package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.time.Duration;

/*package-protected*/ interface IStreamInfo {
    boolean isLiveStream();

    boolean hasStart();
    long getStart(Duration.Unit unit);

    boolean hasChannelId();
    String getChannelId();

    String getPlayMode();
}
