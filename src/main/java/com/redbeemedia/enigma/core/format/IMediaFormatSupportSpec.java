package com.redbeemedia.enigma.core.format;

import java.util.Set;

public interface IMediaFormatSupportSpec {
    boolean supports(EnigmaMediaFormat enigmaMediaFormat);

    Set<EnigmaMediaFormat> getSupportedFormats();
}
