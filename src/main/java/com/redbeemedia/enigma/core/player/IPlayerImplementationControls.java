package com.redbeemedia.enigma.core.player;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IPlayerImplementationControls {
    void load(String url, IPlayerImplementationControlResultHandler resultHandler);
    void start(IPlayerImplementationControlResultHandler resultHandler);
    void pause(IPlayerImplementationControlResultHandler resultHandler);
    void stop(IPlayerImplementationControlResultHandler resultHandler);
    void seekTo(ISeekPosition seekPosition, IPlayerImplementationControlResultHandler resultHandler);
    void setVolume(float volume, IPlayerImplementationControlResultHandler resultHandler);

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
}
