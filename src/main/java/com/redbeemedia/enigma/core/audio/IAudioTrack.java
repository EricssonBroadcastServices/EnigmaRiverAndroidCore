// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.audio;

import com.redbeemedia.enigma.core.track.ITrack;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 */
public interface IAudioTrack extends ITrack {
    String getLabel();
    String getCode();
}
