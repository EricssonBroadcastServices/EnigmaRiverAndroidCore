// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.virtualui;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 */
public interface IVirtualControls {
    IVirtualButton getRewind();
    IVirtualButton getFastForward();
    IVirtualButton getPlay();
    IVirtualButton getPause();
    IVirtualButton getGoToLive();
    IVirtualButton getNextProgram();
    IVirtualButton getPreviousProgram();
    IVirtualButton getSeekBar();
    IVirtualButton getRestart();
    IVirtualButton getSkipIntro();
    IVirtualButton getSubtitlesButton();
    IVirtualButton getAudioTrackButton();
    IVirtualButton getVideoAdClickButton();
}
