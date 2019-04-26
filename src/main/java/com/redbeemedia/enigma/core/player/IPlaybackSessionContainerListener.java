package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.util.IInternalListener;

/*package-protected*/ interface IPlaybackSessionContainerListener extends IInternalListener {
    void onPlaybackSessionChanged(IInternalPlaybackSession oldSession, IInternalPlaybackSession newSession);
}
