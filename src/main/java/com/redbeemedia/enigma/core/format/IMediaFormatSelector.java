// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.format;

import java.util.Collection;

public interface IMediaFormatSelector {
    /**
     * <p>This method will be called by {@link com.redbeemedia.enigma.core.player.EnigmaPlayer EnigmaPlayer} when deciding which manifest to use for playback of
     * an asset.</p>
     *<br/>
     * <p>EnigmaPlayer calls a number of {@code IMediaFormatSelector}s in succession before settling
     * on a selection.
     * Each {@code IMediaFormatSelector} in then chain may change the 'prospect' (the
     * {@link EnigmaMediaFormat} that will be selected after all {@code IMediaFormatSelector}s have
     * been queried) and/or remove available {@link EnigmaMediaFormat}s from the collection
     * {@code available} to prevent those from being selected further down the chain.</p>
     *<br/>
     * <p>If a {@code IMediaFormatSelector} doesn't have a preference among the available
     * {@link EnigmaMediaFormat}s it is typically expected to just return the value passed in the
     * parameter {@code prospect}</p>
     *<br/>
     * @param prospect Current tentative selection. May be {@code null}.
     * @param available A collection of available {@link EnigmaMediaFormat}s to select from.
     *                  Formats may be removed from this collection to prevent chained
     *                  {@code IMediaFormatSelector}s from selecting them.
     * @return The new tentative selection (prospect). If not {@code null}, the collection
     * {@code available} must contain this value.
     */
    EnigmaMediaFormat select(EnigmaMediaFormat prospect, Collection<EnigmaMediaFormat> available);
}
