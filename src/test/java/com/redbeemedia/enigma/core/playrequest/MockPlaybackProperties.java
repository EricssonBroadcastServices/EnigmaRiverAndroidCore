package com.redbeemedia.enigma.core.playrequest;

public class MockPlaybackProperties implements IPlaybackProperties {
    @Override
    public PlayFrom getPlayFrom() {
        return PlayFrom.PLAYER_DEFAULT;
    }
}
