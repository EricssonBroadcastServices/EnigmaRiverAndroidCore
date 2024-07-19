// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.player.DefaultTimelinePositionFactoryTest;
import com.redbeemedia.enigma.core.player.timeline.SimpleTimeline;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MockAdsTestHelpers {

    public static class MockAdsDetectorListener implements IAdDetector.IAdStateListener {
        public boolean adsPlaying;
        /** Increases every time this delegate is being called. */
        public int changeCount;
        public IAd latestAd;
        public AdEventType eventType;
        HashMap<IAd, List<AdEventType>> broadcastedEvents = new HashMap<>();

        @Override
        public void adStateChanged(IAdDetector adsDetector, @Nullable IAd entry, @Nullable AdEventType eventType) {
            adsPlaying = adsDetector.isAdPlaying();
            latestAd = entry;
            this.eventType = eventType;
            changeCount++;
            if(entry == null) {
                broadcastedEvents = new HashMap<>();
            } else {
                if(!broadcastedEvents.containsKey(entry)) { broadcastedEvents.put(entry, new ArrayList<>()); }
                if(!broadcastedEvents.get(entry).contains(entry)) {
                    broadcastedEvents.get(entry).add(eventType);
                }
            }
        }
    }

    /** Boilerplate for initializing the AdDetector. */
    static void initializeLiveAdDetector(AdsLiveVastTests tests) throws IOException, JSONException {

        /* Set up timeline */
        tests.timeline = new SimpleTimeline();
        tests.mockHttpHandler = new MockHttpHandler();
        tests.timeFactory = new DefaultTimelinePositionFactoryTest.TimeLinePositionCreator();
        tests.adStartTime = tests.resourceLoader.loadJSON("ssai.hls.vast.live.response.json").getLong("time");
        tests.manifestTime = 0; // The time the server gives us as a reference
        tests.playerTime = 0; // The local time of the player.

        tests.timeline.setBounds(tests.timeFactory.newPosition(0), tests.timeFactory.newPosition(3600 * 1000 * 1000));
        tests.timeline.setCurrentPosition(tests.timeFactory.newPosition(tests.playerTime));

        /* Set up ad detector */
        tests.adsDetector = new AdDetector(tests.mockHttpHandler, tests.timeline, tests.timeFactory, null);
        tests.listener = new MockAdsDetectorListener();
        tests.adsDetector.addListener(tests.listener);
        tests.hlsLiveResourceLoader = new NowtilusHlsLiveResourceLoader(tests.mockHttpHandler, new NowtilusLiveParser());
        tests.hlsLiveResourceLoader.setManifestUrl("http://www.example.com");
        // Turn on ads detector. It defaults to `false`
        tests.adsDetector.setEnabled(true);
    }
}
