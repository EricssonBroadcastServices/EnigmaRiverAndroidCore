package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import java.util.concurrent.Executor;

/*package-protected*/ class LegacyTimeProvider implements ITimeProvider {
    public LegacyTimeProvider(ISession session) {
        throw new RuntimeException("LegacyTimeProvider not mocked!");
    }

    public com.redbeemedia.enigma.core.player.LegacyTimeProvider startThread(Executor executor) {
        throw new RuntimeException("LegacyTimeProvider not mocked!");
    }

    @Override
    public long getTime() {
        throw new RuntimeException("LegacyTimeProvider not mocked!");
    }
}
