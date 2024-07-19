// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.drm.IDrmProvider;
import com.redbeemedia.enigma.core.format.IMediaFormatSupportSpec;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IEnigmaPlayerEnvironment {
    IDrmProvider getDrmProvider();
    void setMediaFormatSupportSpec(IMediaFormatSupportSpec formatSupportSpec);
    IPlayerImplementationListener getPlayerImplementationListener();
    ITimelinePositionFactory getTimelinePositionFactory();
    void setControls(IPlayerImplementationControls controls);
    void setInternals(IPlayerImplementationInternals internals);
    void addEnigmaPlayerReadyListener(IEnigmaPlayerReadyListener listener);

    interface IEnigmaPlayerReadyListener {
        void onReady(IEnigmaPlayer enigmaPlayer);
    }
}
