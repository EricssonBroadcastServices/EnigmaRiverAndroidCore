package com.redbeemedia.enigma.core.player;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IPlayerImplementationControls {
    void load(String url);
    void start();
    void seekTo(ISeekPosition seekPosition);
    interface ISeekPosition {
        public static ISeekPosition TIMELINE_START = new ISeekPosition() {
        };
    }
}
