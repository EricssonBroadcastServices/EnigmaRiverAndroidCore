// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.session.ISession;

public interface IPlayRequest {
    /**
     * <p>Inspired by {@code org.hamcrest.Matcher} from JUnit lib.</p>
     * <br>
     * <p style="margin-left: 25px; font-weight:bold;">It's easy to ignore JavaDoc, but a bit harder to ignore compile errors .</p>
     * <p style="margin-left: 50px">-- Hamcrest source</p>
     */
    @Deprecated
    void _dont_implement_IPlayRequest___instead_extend_BasePlayRequest_();

    IPlayable getPlayable();
    IPlaybackProperties getPlaybackProperties();
    IPlayResultHandler getResultHandler();
    ISession getSession();
}
