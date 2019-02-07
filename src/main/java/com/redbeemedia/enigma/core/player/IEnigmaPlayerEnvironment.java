package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.drm.IDrmProvider;
import com.redbeemedia.enigma.core.format.IMediaFormatSupportSpec;

public interface IEnigmaPlayerEnvironment {
    IDrmProvider getDrmProvider();
    void setMediaFormatSupportSpec(IMediaFormatSupportSpec formatSupportSpec);
}
