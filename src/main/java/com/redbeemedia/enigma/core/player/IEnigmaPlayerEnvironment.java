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
    void setControls(IPlayerImplementationControls controls);
    void setInternals(IPlayerImplementationInternals internals);
}
