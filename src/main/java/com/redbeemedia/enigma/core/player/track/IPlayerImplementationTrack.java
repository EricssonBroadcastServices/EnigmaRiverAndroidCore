// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player.track;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.video.IVideoTrack;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 *
 * <p>
 * Every 'as'-method returns null if not available as that type. <br>
 * For example, <code>asSubtitleTrack()</code> on a video-track would return <code>null</code>.
 * </p>
 */
public interface IPlayerImplementationTrack {
    ISubtitleTrack asSubtitleTrack();
    IAudioTrack asAudioTrack();
    IVideoTrack asVideoTrack();
}
