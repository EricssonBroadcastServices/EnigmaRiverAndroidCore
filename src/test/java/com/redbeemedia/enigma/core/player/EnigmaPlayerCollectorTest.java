package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.testutil.Counter;

import org.junit.Test;

public class EnigmaPlayerCollectorTest {
    @Test
    public void testOnError() {
        EnigmaPlayerCollector collector = new EnigmaPlayerCollector();
        final Counter onPlaybackErrorCalled = new Counter();
        BaseEnigmaPlayerListener listener = new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackError(Error error) {
                onPlaybackErrorCalled.count();
            }
        };
        collector.addListener(listener);
        collector.onPlaybackError(new UnexpectedError("Test"));
        onPlaybackErrorCalled.assertOnce();
        collector.removeListener(listener);
        collector.onPlaybackError(new UnexpectedError("Test 2"));
        onPlaybackErrorCalled.assertOnce();
    }
}
