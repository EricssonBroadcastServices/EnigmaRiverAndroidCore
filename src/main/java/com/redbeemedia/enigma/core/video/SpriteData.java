// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.video;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.time.Duration;

import java.net.URL;
import java.util.Locale;

/** Represents video sprite metadata for a video stream.
    A SpriteData model typically contains a reference to an image that in turn contains several frames.
    Each SpriteData model contains a bounding rectangle containing the part of the image that
    contains the actual video frame.
 */
public class SpriteData {

    /** The duration of this frame. */
    public final Duration duration;
    /** The absolute position in the stream for this frame. */
    public final ITimelinePosition position;
    /** The base image url containing a set of frames. */
    public final URL imageUrl;
    /** The part of the image retrieved via `imageUrl` containing the actual fram. */
    public final Frame frame;

    public SpriteData(Duration duration, ITimelinePosition position, URL imageUrl, Frame frame) {
        this.duration = duration;
        this.position = position;
        this.imageUrl = imageUrl;
        this.frame = frame;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SpriteData)) { return false; }
        SpriteData other = (SpriteData)obj;
        return other.position == this.position && other.duration == this.duration && other.frame == this.frame;
    }

    @Override
    public int hashCode() {
        return String.format(Locale.getDefault(),"%d#%d", imageUrl.hashCode(), frame.hashCode()).hashCode();
    }

    /** Represents a rectangle. */
    public static class Frame {
        public final int x;
        public final int y;
        public final int width;
        public final int height;

        public Frame(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public static final Frame zero = new Frame(0,0,0, 0);

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Frame)) { return false; }
            Frame other = (Frame)obj;
            return other.x == x && other.y == y && other.width == width && other.height == height;
        }

        @Override
        public int hashCode() {
            return String.format(Locale.getDefault(),"%d,%d,%d,%d", x, y, width, height).hashCode();
        }
    }
}
