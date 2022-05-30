package com.redbeemedia.enigma.core.player.listener;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.EnigmaPlayerState;
import com.redbeemedia.enigma.core.util.IInternalListener;

public interface IEnigmaPlayerListener extends IInternalListener {
    /**
     * <p>Inspired by {@code org.hamcrest.Matcher} from JUnit lib.</p>
     * <br>
     * <p style="margin-left: 25px; font-weight:bold;">It's easy to ignore JavaDoc, but a bit harder to ignore compile errors .</p>
     * <p style="margin-left: 50px">-- Hamcrest source</p>
     */
    @Deprecated
    void _dont_implement_IEnigmaPlayerListener___instead_extend_BaseEnigmaPlayerListener_();

    void onPlaybackError(EnigmaError error); //An error occurred during playback

    void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to);

    void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to);

    void onProgramChanged(IProgram from, IProgram to);

    void sendPlaybackStartedEvent();
}
