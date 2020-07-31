package com.redbeemedia.enigma.core.format;

import java.util.Collection;

public final class ChainedMediaFormatSelector implements IMediaFormatSelector {
    private final IMediaFormatSelector[] selectors;

    public ChainedMediaFormatSelector(IMediaFormatSelector ... selectors) {
        this.selectors = selectors;
    }

    @Override
    public EnigmaMediaFormat select(EnigmaMediaFormat prospect, Collection<EnigmaMediaFormat> available) {
        for(IMediaFormatSelector selector : selectors) {
            if(selector != null) {
                prospect = selector.select(prospect, available);
            }
        }
        return prospect;
    }
}
