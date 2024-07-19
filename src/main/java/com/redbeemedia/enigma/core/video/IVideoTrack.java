// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.video;

import com.redbeemedia.enigma.core.track.ITrack;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 */
public interface IVideoTrack extends ITrack {
    /**
     * @return Bitrate in bits per second or -1 if N/A
     */
    int getBitrate();

    /**
     * @return Width in pixels or -1 if N/A
     */
    int getWidth();

    /**
     * @return Height in pixels or -1 if N/A
     */
    int getHeight();
}
