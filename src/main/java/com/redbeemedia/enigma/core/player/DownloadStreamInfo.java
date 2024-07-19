// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.time.Duration;

/*package-protected*/ class DownloadStreamInfo implements IStreamInfo {

    @Override
    public boolean isLiveStream() {
        return false;
    }

    @Override
    public boolean isEvent() { return false; }

    @Override
    public boolean hasStart() {
        return false;
    }

    @Override
    public long getStart(Duration.Unit unit) {
        return 0;
    }

    @Override
    public boolean hasChannelId() {
        return false;
    }

    @Override
    public String getChannelId() {
        return null;
    }

    @Override
    public String getPlayMode() {
        return "offline";
    }

    @Override
    public boolean ssaiEnabled() { return false; }

    @Override
    public Duration getLiveDelay() {
        return null;
    }

    @Override
    public MediaType getMediaType() {
        return null;
    }
}
