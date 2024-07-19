// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.util.Collector;

/*package-protected*/ class PlaybackSessionContainerCollector extends Collector<IPlaybackSessionContainerListener> implements IPlaybackSessionContainerListener {
    public PlaybackSessionContainerCollector() {
        super(IPlaybackSessionContainerListener.class);
    }

    @Override
    public void onPlaybackSessionChanged(IInternalPlaybackSession oldSession, IInternalPlaybackSession newSession) {
        forEach(listener -> listener.onPlaybackSessionChanged(oldSession, newSession));
    }
}
