package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.util.IInternalCallbackObject;

public interface IPlayResultHandler extends IInternalCallbackObject {
    /**
     * <p>Inspired by {@code org.hamcrest.Matcher} from JUnit lib.</p>
     * <br>
     * <p style="margin-left: 25px; font-weight:bold;">It's easy to ignore JavaDoc, but a bit harder to ignore compile errors .</p>
     * <p style="margin-left: 50px">-- Hamcrest source</p>
     */
    @Deprecated
    void _dont_implement_IPlayResultHandler___instead_extend_BasePlayResultHandler_();

    void onStarted(IPlaybackSession playbackSession);
    void onError(Error error);
}
