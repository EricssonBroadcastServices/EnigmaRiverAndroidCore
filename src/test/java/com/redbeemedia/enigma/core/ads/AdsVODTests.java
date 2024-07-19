// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.player.DefaultTimelinePositionFactoryTest;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.timeline.SimpleTimeline;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.TestResourceLoader;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class AdsVODTests {

    TestResourceLoader resourceLoader = new TestResourceLoader(getClass());
    DefaultTimelinePositionFactoryTest.TimeLinePositionCreator timeFactory = new DefaultTimelinePositionFactoryTest.TimeLinePositionCreator();

    private long startMs() { return 1; }
    private long firstQMs(long duration) { return (long)(1000 * duration * 0.25 + 1.0); }
    private long midpointMs(long duration) { return (long)(1000 * duration * 0.5 + 1.0); }
    private long thirdQMs(long duration) { return (long)(1000 * duration * 0.75 + 1.0); }
    private long completeMs(long duration) { return 1000 * duration; }

    private long startMs(long startPosition) { return 1000 * startPosition; }
    private long firstQMs(long start, long duration) { return 1000 * start + firstQMs(duration); }
    private long midpointMs(long start, long duration) { return 1000 * start + midpointMs(duration); }
    private long thirdQMs(long start, long duration) { return 1000 * start + thirdQMs(duration); }
    private long completeMs(long start, long duration) { return 1000 * start + completeMs(duration); }

    @Test
    public void getPastAdDurationIfCurrentPositionHasNoAds() throws IOException, JSONException {
        /* Set up timeline and ads factory */
        SimpleTimeline realTimeline = new SimpleTimeline();

        long adBreak1End = 25000; // end of first ad block
        long adBreak2End = 25000 + 30030 + 27000; // end of second ad block
        long totalAdLength = 25000 + 27000 + 25000; // The total length of all the ads.
        AdDetector adsDetector = createAdDetector(realTimeline, new MockHttpHandler());
        IAdResourceLoader adResourceLoader = createResourceLoader(adsDetector, "ssai.hls.vod.play.response.json");
        adsDetector.update(adResourceLoader, 0);
        AdIncludedTimeline timeline = new AdIncludedTimeline(realTimeline, adsDetector);

        int adForGivenTime = timeline.getCountOfAdsPassed(timeFactory.newPosition(42000));

        assertThat(adForGivenTime,is(2));

        adForGivenTime = timeline.getCountOfAdsPassed(timeFactory.newPosition(30000));
        assertThat(adForGivenTime,is(1));

        adForGivenTime = timeline.getCountOfAdsPassed(timeFactory.newPosition(30030));
        assertThat(adForGivenTime,is(1));

        adForGivenTime = timeline.getCountOfAdsPassed(timeFactory.newPosition(72030));
        assertThat(adForGivenTime,is(2));

        adForGivenTime = timeline.getCountOfAdsPassed(timeFactory.newPosition(72150));
        assertThat(adForGivenTime,is(3));

        long time = timeline.getTotalAdDurationFromThisTime(timeFactory.newPosition(30000));
        assertThat(time,is(adBreak1End));
        time = timeline.getTotalAdDurationFromThisTime(timeFactory.newPosition(0));
        assertThat(time,is(adBreak1End));

        time = timeline.getTotalAdDurationFromThisTime(timeFactory.newPosition(72000));
        assertThat(time,is(25000l + 27000l));

        time = timeline.getTotalAdDurationFromThisTime(timeFactory.newPosition(82000));
        assertThat(time,is(25000l + 27000l + 25000l));
    }

    @Test
    public void testGetLastAdPositionFromThisPosition() throws IOException, JSONException {
        /* Set up timeline and ads factory */
        SimpleTimeline realTimeline = new SimpleTimeline();

        long adBreak1End = 25000; // end of first ad block
        long adBreak2End = 25000 + 30030 + 27000; // end of second ad block
        long totalAdLength = 25000 + 27000 + 25000; // The total length of all the ads.
        AdDetector adsDetector = createAdDetector(realTimeline, new MockHttpHandler());
        IAdResourceLoader adResourceLoader = createResourceLoader(adsDetector, "ssai.hls.vod.play.response.json");
        adsDetector.update(adResourceLoader, 0);
        AdIncludedTimeline timeline = new AdIncludedTimeline(realTimeline, adsDetector);

        ITimelinePosition currentPosition = timeFactory.newPosition(30000);
        ITimelinePosition lastAdPositionFromThisPosition = timeline.getLastAdPositionFromThisPosition(currentPosition);
        assertThat(lastAdPositionFromThisPosition.getStart(), is(0l));

        currentPosition = timeFactory.newPosition(83000);
        lastAdPositionFromThisPosition = timeline.getLastAdPositionFromThisPosition(currentPosition);
        assertThat(lastAdPositionFromThisPosition.getStart(), is(55030L));
    }

    @Test
    public void testGetAdBreakIfPositionIsBetweenTheAd() throws IOException, JSONException {
        /* Set up timeline and ads factory */
        SimpleTimeline realTimeline = new SimpleTimeline();

        long adBreak1End = 25000; // end of first ad block
        long adBreak2End = 25000 + 30030 + 27000; // end of second ad block
        long totalAdLength = 25000 + 27000 + 25000; // The total length of all the ads.
        AdDetector adsDetector = createAdDetector(realTimeline, new MockHttpHandler());
        IAdResourceLoader adResourceLoader = createResourceLoader(adsDetector, "ssai.hls.vod.play.response.json");
        adsDetector.update(adResourceLoader, 0);
        AdIncludedTimeline timeline = new AdIncludedTimeline(realTimeline, adsDetector);

        ITimelinePosition currentPosition = timeFactory.newPosition(22000);
        AdBreak adBreak = timeline.getAdBreakIfPositionIsBetweenTheAd(currentPosition);
        assertThat(adBreak,is(adsDetector.getAdBreaks().get(0)));


         currentPosition = timeFactory.newPosition(26000);
         adBreak = timeline.getAdBreakIfPositionIsBetweenTheAd(currentPosition);
        assertNull(adBreak);

        currentPosition = timeFactory.newPosition(57000);
        adBreak = timeline.getAdBreakIfPositionIsBetweenTheAd(currentPosition);
        assertThat(adBreak,is(adsDetector.getAdBreaks().get(1)));
    }


    @Test
    public void testGetLastAdBreakFromThisPosition() throws IOException, JSONException {
        /* Set up timeline and ads factory */
        SimpleTimeline realTimeline = new SimpleTimeline();

        long adBreak1End = 25000; // end of first ad block
        long adBreak2End = 25000 + 30030 + 27000; // end of second ad block
        long totalAdLength = 25000 + 27000 + 25000; // The total length of all the ads.
        AdDetector adsDetector = createAdDetector(realTimeline, new MockHttpHandler());
        IAdResourceLoader adResourceLoader = createResourceLoader(adsDetector, "ssai.hls.vod.play.response.json");
        adsDetector.update(adResourceLoader, 0);
        AdIncludedTimeline timeline = new AdIncludedTimeline(realTimeline, adsDetector);

        ITimelinePosition currentPosition = timeFactory.newPosition(30000);
        AdBreak lastAdBreakFromThisPosition = timeline.getLastAdBreakFromThisPosition(currentPosition);
        assertThat(lastAdBreakFromThisPosition, is(adsDetector.getAdBreaks().get(0)));

        currentPosition = timeFactory.newPosition(45000);
        lastAdBreakFromThisPosition = timeline.getLastAdBreakFromThisPosition(currentPosition);
        assertThat(lastAdBreakFromThisPosition, is(adsDetector.getAdBreaks().get(0)));

        currentPosition = timeFactory.newPosition(57030);
        lastAdBreakFromThisPosition = timeline.getLastAdBreakFromThisPosition(currentPosition);
        assertThat(lastAdBreakFromThisPosition, is(adsDetector.getAdBreaks().get(1)));

        currentPosition = timeFactory.newPosition(82000);
        lastAdBreakFromThisPosition = timeline.getLastAdBreakFromThisPosition(currentPosition);
        assertThat(lastAdBreakFromThisPosition, is(adsDetector.getAdBreaks().get(1)));

        currentPosition = timeFactory.newPosition(83000);
        lastAdBreakFromThisPosition = timeline.getLastAdBreakFromThisPosition(currentPosition);
        assertThat(lastAdBreakFromThisPosition, is(adsDetector.getAdBreaks().get(1)));


        currentPosition = timeFactory.newPosition(125072);
        lastAdBreakFromThisPosition = timeline.getLastAdBreakFromThisPosition(currentPosition);
        assertThat(lastAdBreakFromThisPosition, is(adsDetector.getAdBreaks().get(2)));
    }

    @Test
    public void testVastJsonParser() throws IOException, JSONException {

        // Load the vast manifest in the state that it will be retrieved from a server.
        JSONObject vastResponse = resourceLoader.loadJSON("ssai.hls.vod.play.response.2.json").getJSONObject("ads");

        NowtilusVodParser parser = new NowtilusVodParser();

        VastAdEntrySet set = parser.parseEntries(vastResponse);

        Assert.assertNotNull(set);

        long firstAdDuration = 5;
        long secondAdDuration = 10;
        long firstContentDuration = 30;
        long thirdAdDuration = 20;
        long firstAdSectionEnd = firstAdDuration + secondAdDuration; // 5 seconds + 10 seconds
        long firstContentSectionEnd = firstAdSectionEnd + firstContentDuration; // ~30 sec content.
        long secondAdSectionStart = firstContentSectionEnd;

        VastImpression firstLoadedEvent = new ArrayList<>(set.getEntries()).get(0).getEntrySet(AdEventType.Loaded);
        VastImpression firstStartEvent = new ArrayList<>(set.getEntries()).get(0).getEntrySet(AdEventType.Start);
        Assert.assertNull(firstLoadedEvent);
        Assert.assertEquals(firstStartEvent.getUrls().size(),7);

        Assert.assertEquals(AdEventType.Start, set.getEntry(startMs()).getImpression().type);
        Assert.assertEquals(AdEventType.FirstQuartile, set.getEntry(firstQMs(0, firstAdDuration)).getImpression().type);
        Assert.assertEquals(AdEventType.MidPoint, set.getEntry(midpointMs(0, firstAdDuration)).getImpression().type);
        Assert.assertEquals(AdEventType.ThirdQuartile, set.getEntry(thirdQMs(0, firstAdDuration)).getImpression().type);
        Assert.assertEquals(AdEventType.Complete, set.getEntry(completeMs(0, firstAdDuration)).getImpression().type);

        Assert.assertEquals(AdEventType.Start, set.getEntry(startMs()).getImpression(firstAdDuration).type); // Start is missing.
        Assert.assertEquals(AdEventType.FirstQuartile, set.getEntry(firstQMs(firstAdDuration, secondAdDuration)).getImpression().type);
        Assert.assertEquals(AdEventType.MidPoint, set.getEntry(midpointMs(firstAdDuration, secondAdDuration)).getImpression().type);
        Assert.assertEquals(AdEventType.ThirdQuartile, set.getEntry(thirdQMs(firstAdDuration, secondAdDuration)).getImpression().type);
        Assert.assertEquals(AdEventType.Complete, set.getEntry(completeMs(firstAdDuration, secondAdDuration)).getImpression().type);

        // Content
        Assert.assertNull(set.getEntry(completeMs(firstAdSectionEnd) + 1)); // Just after last ad in first ad section.
        Assert.assertNull(set.getEntry(completeMs(firstContentSectionEnd) - 1)); // Just before the first content section ends.

        Assert.assertEquals(AdEventType.Start, set.getEntry(startMs()).getImpression(secondAdSectionStart).type); // Start is missing.
        Assert.assertEquals(AdEventType.FirstQuartile, set.getEntry(firstQMs(secondAdSectionStart, thirdAdDuration)).getImpression().type);
        Assert.assertEquals(AdEventType.MidPoint, set.getEntry(midpointMs(secondAdSectionStart, thirdAdDuration)).getImpression().type);
        Assert.assertEquals(AdEventType.ThirdQuartile, set.getEntry(thirdQMs(secondAdSectionStart, thirdAdDuration)).getImpression().type);
        Assert.assertEquals(AdEventType.Complete, set.getEntry(completeMs(secondAdSectionStart, thirdAdDuration)).getImpression().type);
    }

    public IAdResourceLoader createResourceLoader(IAdDetector adsDetector, String adsJsonFileName) throws IOException, JSONException {
        JSONObject playResponse = resourceLoader.loadJSON(adsJsonFileName);
        JSONObject mediaFormat = playResponse.getJSONArray("formats").getJSONObject(0);
        IAdMetadata adsnfo = new ExposureAdMetadata(playResponse.getJSONObject("ads"), EnigmaMediaFormat.parseMediaFormat(mediaFormat).getStreamFormat(), false);
        IAdResourceLoader adResourceLoader = adsDetector.getFactory().createResourceLoader(adsnfo, playResponse.getJSONObject("ads"));
        Assert.assertNotNull(adResourceLoader);
        Assert.assertEquals(IAdMetadata.AdStitcherType.Nowtilus, adsnfo.getStitcher());
        Assert.assertEquals(adResourceLoader.getClass(), NowtilusVodResourceLoader.class);
        return adResourceLoader;
    }

    public AdDetector createAdDetector(SimpleTimeline timeline, MockHttpHandler mockHttpHandler) throws IOException, JSONException {

        timeline.setBounds(timeFactory.newPosition(0), timeFactory.newPosition(3600 * 1000));
        timeline.setCurrentPosition(timeFactory.newPosition(0));

        /* Fetch ads detector */
        AdDetector adsDetector = new AdDetector(mockHttpHandler, timeline, timeFactory);

        // Turn on ads detector. It defaults to `false`
        adsDetector.setEnabled(true);
        return adsDetector;
    }

    @Test
    public void testAdFactoryAndDetector() throws IOException, JSONException {

        /* Set up timeline and ads factory */
        SimpleTimeline timeline = new SimpleTimeline();
        MockHttpHandler mockHttpHandler = new MockHttpHandler();

        long playerTime = 0; // The local time of the player.
        AdDetector adsDetector = createAdDetector(timeline, mockHttpHandler);
        IAdResourceLoader adResourceLoader = createResourceLoader(adsDetector,"ssai.hls.vod.play.response.json");

        MockAdsTestHelpers.MockAdsDetectorListener listener = new MockAdsTestHelpers.MockAdsDetectorListener();
        adsDetector.addListener(listener);

        /* Start test */

        // Make a manifest check. We should not receive any ads.
        mockHttpHandler.queueResponse(new HttpStatus(204, null));

        adsDetector.update(adResourceLoader, 0);
        Assert.assertEquals(0, listener.changeCount);
        Assert.assertFalse(listener.adsPlaying);

        mockHttpHandler.keepLastResponse = true;
        // Fetch the manifest. Since the first ad start at position 0, the ad should be considered to be playing once the timeline has been updated.
        mockHttpHandler.queueResponseOk(Pattern.compile(".*"), resourceLoader.loadString("ssai.hls.vod.play.response.json"));
        adsDetector.update(adResourceLoader, 0);
        // Timeline not updated, so no ad has been detected:
        Assert.assertEquals(0, listener.changeCount);
        Assert.assertFalse(listener.adsPlaying);
        Assert.assertNull(listener.latestAd);

        // Timeline position changed and delegate should have been called.
        playerTime += 1;
        int requestsSent = 3; // start of ad has 3 url
        timeline.setCurrentPosition(timeFactory.newPosition(playerTime)); // Trigger listener with the first ad.
        Assert.assertEquals(1, listener.changeCount);
        Assert.assertTrue(listener.adsPlaying);
        Assert.assertNotNull(listener.latestAd);
        IAd latestAd = listener.latestAd;
        // First ad does not have a start event, so no beacons sent.
        Assert.assertEquals(requestsSent, mockHttpHandler.getLog().size());

        // Move to a location in the first quartile of the first ad:
        playerTime += 2000;
        timeline.setCurrentPosition(timeFactory.newPosition(playerTime));
        // Only the "null" event for the listeners should not have been triggered, since the ad was not new.
        Assert.assertEquals(2, listener.changeCount);
        Assert.assertTrue(listener.adsPlaying);
        Assert.assertNotNull(listener.latestAd);
        Assert.assertEquals(latestAd, listener.latestAd); // Still the first ad.
        latestAd = listener.latestAd;
        // First ad does have a firstQuartile event (2 beacons).
        Assert.assertEquals(requestsSent + 2, mockHttpHandler.getLog().size());
        requestsSent = mockHttpHandler.getLog().size();

        // Move to a location in the last quartile of the second ad:
        playerTime += latestAd.getDuration();
        timeline.setCurrentPosition(timeFactory.newPosition(playerTime));
        // New ad. Listeners should have been called once.
        //TODO: ask for id diff!
        Assert.assertEquals(3, listener.changeCount);
        Assert.assertTrue(listener.adsPlaying);
        Assert.assertNotEquals(latestAd, listener.latestAd); // Now, the second ad is playing.

        // Second ad does have a firstQuartile event (2 beacons).
        Assert.assertEquals(requestsSent + 2, mockHttpHandler.getLog().size());
        requestsSent = mockHttpHandler.getLog().size();

        /* Test behavior during "content playback". */
        // Move to content time (outside ads).
        playerTime += 20000;

        timeline.setCurrentPosition(timeFactory.newPosition(playerTime));
        // Listener should be called once (changed from ad to no ad).
        Assert.assertEquals(4, listener.changeCount);
        Assert.assertFalse(listener.adsPlaying);
        Assert.assertNull(listener.latestAd);

        // No new requests should have been sent
        Assert.assertEquals(requestsSent, mockHttpHandler.getLog().size());

        // We will be playing outside ad space and stop at 54 seconds (1 second before the first ad in the second ad sequence begins).
        for(int i = 27000; i <= 54000; i += 1000) {
            playerTime = i;
            timeline.setCurrentPosition(timeFactory.newPosition(playerTime));
            // Delegate should not have been called, since.
            Assert.assertEquals(4, listener.changeCount);
            Assert.assertFalse(listener.adsPlaying);
            Assert.assertNull(listener.latestAd); // No ad is being played.
            // No new requests should have been sent.
            Assert.assertEquals(requestsSent, mockHttpHandler.getLog().size());
        }

        /* Ads start playing again */
        playerTime += 1000 + 3000; // Midpoint into the first ad in the second ad sequence.
        timeline.setCurrentPosition(timeFactory.newPosition(playerTime));
        // New ad. Listeners should have been called.
        Assert.assertEquals(5, listener.changeCount);
        Assert.assertTrue(listener.adsPlaying);
        Assert.assertNotNull(listener.latestAd);

        // This ad have a midpoint event (4 beacons).
        Assert.assertEquals(requestsSent + 4, mockHttpHandler.getLog().size());
    }

    @Test
    public void testAdBreaks() throws IOException, JSONException {

        SimpleTimeline timeline = new SimpleTimeline();

        AdDetector adsDetector = createAdDetector(timeline, new MockHttpHandler());
        IAdResourceLoader adResourceLoader = createResourceLoader(adsDetector,"ssai.hls.vod.play.response.json");
        adsDetector.update(adResourceLoader, 0);
        List<AdBreak> breaks = adsDetector.getAdBreaks();

        // 3 ad breaks should have been identified
        Assert.assertEquals(3, breaks.size());

        // Check the first ad break
        Assert.assertEquals(timeFactory.newPosition(0), breaks.get(0).start);
        Assert.assertEquals(Duration.millis(25 * 1000), breaks.get(0).duration);

        // Check the second ad break
        Assert.assertEquals(timeFactory.newPosition( 30030).add(breaks.get(0).duration), breaks.get(1).start);
        Assert.assertEquals(Duration.millis(27 * 1000), breaks.get(1).duration);

        // Check the third ad break
        Assert.assertEquals(timeFactory.newPosition( 30030 + 42042).add(breaks.get(0).duration).add(breaks.get(1).duration), breaks.get(2).start);
        Assert.assertEquals(Duration.millis(25 * 1000), breaks.get(2).duration);

    }

    @Test
    public void testAdInfestedTimeline() throws IOException, JSONException {

        /* Set up timeline and ads factory */
        SimpleTimeline realTimeline = new SimpleTimeline();

        long adBreak1End = 25000; // end of first ad block
        long adBreak2End = 25000 + 30030 + 27000; // end of second ad block
        long totalAdLength = 25000 + 27000 + 25000; // The total length of all the ads.
        AdDetector adsDetector = createAdDetector(realTimeline, new MockHttpHandler());
        IAdResourceLoader adResourceLoader = createResourceLoader(adsDetector, "ssai.hls.vod.play.response.json");

        AdIncludedTimeline timeline = new AdIncludedTimeline(realTimeline, adsDetector);
        timeline.setIsActive(true);

        //No ad breaks loaded
        Assert.assertNull(timeline.getAdBreaksPositions());

        adsDetector.update(adResourceLoader, 0);

        // Fetch the points in the timeline containing the perceived (transposed) ad breaks
        Assert.assertEquals(3, timeline.getAdBreaksPositions().size());
        Assert.assertEquals(3, timeline.getAdBreaks().size());
        // Check that the transposed ad breaks are at the expected positions (matching the content parts of the ads
        Assert.assertEquals(timeFactory.newPosition(0), timeline.getAdBreaksPositions().get(0));
        Assert.assertEquals(timeFactory.newPosition(30030), timeline.getAdBreaksPositions().get(1));
        Assert.assertEquals(timeFactory.newPosition(30030 + 42042), timeline.getAdBreaksPositions().get(2));

        List<AdBreak> breaks = adsDetector.getAdBreaks();

        Assert.assertEquals(timeFactory.newPosition(0), timeline.getCurrentPosition());
        Assert.assertEquals(timeFactory.newPosition(0), timeline.getCurrentStartBound());
        Assert.assertEquals(timeFactory.newPosition(0), timeline.getCurrentAdBreak().start);

        Duration perceivedEndBound = realTimeline.getCurrentEndBound().subtract(timeFactory.newPosition(totalAdLength));
        Assert.assertEquals(perceivedEndBound, timeline.getCurrentEndBound().subtract(timeline.getCurrentStartBound()));

        // Playback has not started
        for(int i = 0; i < breaks.size(); i++) {
            Assert.assertFalse(breaks.get(i).getIsFinished());
        }

        // Move half past the part of the first ad break
        for(int i = 0; i < adBreak1End / 2; i = i + 100) { realTimeline.setCurrentPosition(timeFactory.newPosition(i)); }
        // Ad break has not been watched to the end
        Assert.assertFalse(breaks.get(0).getIsFinished());
        // Move past the entire first ad break
        for(int i = (int)adBreak1End / 2; i < adBreak1End + 2000; i = i + 100) { realTimeline.setCurrentPosition(timeFactory.newPosition(i)); }
        // The ads should be done by now.
        Assert.assertTrue(breaks.get(0).getIsFinished());


        // Set the position to the after the first ad.
        long perceivedPosition = 3000;
        long realPosition = adBreak1End + perceivedPosition;
        realTimeline.setCurrentPosition(timeFactory.newPosition(realPosition));
        Assert.assertEquals(timeFactory.newPosition(perceivedPosition), timeline.getCurrentPosition());
        Assert.assertNull(timeline.getCurrentAdBreak());

        // Set the position in the end of the first ad.
        perceivedPosition = 0;
        realPosition = adBreak1End - 1000;
        realTimeline.setCurrentPosition(timeFactory.newPosition(realPosition));
        Assert.assertEquals(timeFactory.newPosition(perceivedPosition), timeline.getCurrentPosition());
        Assert.assertEquals(timeFactory.newPosition(0), timeline.getCurrentAdBreak().start);

        // Set the position in the end of the second ad.
        perceivedPosition = 30030;
        realPosition = adBreak2End - 1000;
        realTimeline.setCurrentPosition(timeFactory.newPosition(realPosition));
        Assert.assertEquals(timeFactory.newPosition(perceivedPosition), timeline.getCurrentPosition());
        Assert.assertEquals(timeFactory.newPosition(30030), timeline.getCurrentAdBreak().start);

        // Set the position in the content section afther the second ad.
        perceivedPosition = 33030;
        realPosition = adBreak2End + 3000;
        realTimeline.setCurrentPosition(timeFactory.newPosition(realPosition));
        Assert.assertEquals(timeFactory.newPosition(perceivedPosition), timeline.getCurrentPosition());
        Assert.assertNull(timeline.getCurrentAdBreak());
    }


}
