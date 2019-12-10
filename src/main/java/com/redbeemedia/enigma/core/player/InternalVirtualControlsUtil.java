package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class InternalVirtualControlsUtil {
    public static boolean hasStreamPrograms(IPlaybackSession playbackSession) {
        if(playbackSession == null) {
            return false;
        }
        return ((IInternalPlaybackSession) playbackSession).getStreamPrograms() != null;
    }

    public static boolean hasPlaybackSessionSeed(IEnigmaPlayer enigmaPlayer) {
        if(enigmaPlayer instanceof EnigmaPlayer) {
            return ((EnigmaPlayer) enigmaPlayer).hasPlaybackSessionSeed();
        }
        return false;
    }
}
