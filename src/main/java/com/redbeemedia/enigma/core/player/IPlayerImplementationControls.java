// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.video.IVideoTrack;

import java.util.Map;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IPlayerImplementationControls {
    void load(ILoadRequest loadRequest, IPlayerImplementationControlResultHandler resultHandler);
    void start(IPlayerImplementationControlResultHandler resultHandler);
    void pause(IPlayerImplementationControlResultHandler resultHandler);
    void stop(IPlayerImplementationControlResultHandler resultHandler);
    void seekTo(ISeekPosition seekPosition, IPlayerImplementationControlResultHandler resultHandler);
    void setVolume(float volume, IPlayerImplementationControlResultHandler resultHandler);
    void setSubtitleTrack(ISubtitleTrack track, IPlayerImplementationControlResultHandler resultHandler);
    void setAudioTrack(IAudioTrack track, IPlayerImplementationControlResultHandler resultHandler);
    void setVideoTrack(IVideoTrack track, final IPlayerImplementationControlResultHandler resultHandler);
    void setMaxVideoTrackDimensions(int width, int height, IPlayerImplementationControlResultHandler controlResultHandler);
    Map<String,String> getDrmKeyStatusMap();

    interface ISeekPosition {
        public static ISeekPosition TIMELINE_START = new ISeekPosition() {
        };
        public static ISeekPosition LIVE_EDGE = new ISeekPosition() {
        };
    }
    class TimelineRelativePosition implements ISeekPosition {
        private final long millis;

        public TimelineRelativePosition(long millis) {
            this.millis = millis;
        }

        public long getMillis() {
            return millis;
        }
    }

    interface ILoadRequest {
        /** <code>null</code> indicates no restriction. */
        Integer getMaxBitrate();
        /** <code>null</code> indicates no restriction. */
        Integer getMaxResoultionHeight();

        /** <code>null</code> indicates no special request. **/
        Duration getLiveDelay();
    }

    interface IStreamLoadRequest extends ILoadRequest {
        String getUrl();
    }

    interface IDownloadedLoadRequest extends ILoadRequest {
        Object getDownloadData();
    }
}
