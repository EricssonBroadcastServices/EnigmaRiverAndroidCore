// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.ads.IAdDetector;
import com.redbeemedia.enigma.core.analytics.IAnalyticsReporter;
import com.redbeemedia.enigma.core.marker.IMarkerPointsDetector;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.virtualui.IVirtualControls;

public interface IEnigmaPlayer {
    void play(IPlayRequest playRequest);
    boolean addListener(IEnigmaPlayerListener playerListener);
    boolean addListener(IEnigmaPlayerListener playerListener, Handler handler);
    boolean removeListener(IEnigmaPlayerListener playerListener);
    IEnigmaPlayerControls getControls();
    ITimeline getTimeline();
    EnigmaPlayerState getState();
    IAdDetector getAdDetector();
    IMarkerPointsDetector getMarkerPointsDetector();
    IEnigmaPlayer setCallbackHandler(IHandler handler);
    IEnigmaPlayer setCallbackHandler(Handler handler);
    void setDefaultSession(ISession session);
    boolean isLiveStream();
    void release();
    boolean isAdBeingPlayed();
    IVirtualControls getVirtualControls();
    void setVirtualControls(IVirtualControls virtualControls);
    IAnalyticsReporter getCurrentAnalyticsReporter();

    /**
     * Set it true to enable sticky player functionality.
     * Failing to set this value true, will release the player as soon as the PlayerActivity will be destroyed
     *
     * @param isStickyPlayer
     */
    void setStickyPlayer(boolean isStickyPlayer);

    /**
     * return is sticky player functionality has been set via app
     * @return
     */
    boolean isStickyPlayer();

    /**
     *
     * @return 'true' if the asset is being played and current asset is audio-only
     */
    boolean isCurrentStreamTypeAudioOnly();
}
