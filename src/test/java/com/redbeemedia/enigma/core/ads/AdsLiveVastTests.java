package com.redbeemedia.enigma.core.ads;

import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.player.DefaultTimelinePositionFactoryTest;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.timeline.SimpleTimeline;
import com.redbeemedia.enigma.core.util.TestResourceLoader;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class AdsLiveVastTests {

    TestResourceLoader resourceLoader = new TestResourceLoader(getClass());

    SimpleTimeline timeline;
    MockHttpHandler mockHttpHandler;
    DefaultTimelinePositionFactoryTest.TimeLinePositionCreator timeFactory = new DefaultTimelinePositionFactoryTest.TimeLinePositionCreator();
    long adStartTime = 0;
    long manifestTime = 0; // The time the server gives us as a reference
    long playerTime = 0; // The local time of the player.

    AdDetector adsDetector;
    MockAdsTestHelpers.MockAdsDetectorListener listener;
    NowtilusHlsLiveResourceLoader hlsLiveResourceLoader;

    /**
     * Test parsing of a VAST XML document.
     * Test the fetching of impressions.
     */
    @Test
    public void testVastHLSXmlParsing() throws IOException, JSONException {
        // Load the vast manifest in the state that it will be retrieved from a server.
        JSONObject vastResponse = resourceLoader.loadJSON("ssai.hls.vast.live.response.json");

        NowtilusLiveParser parser = new NowtilusLiveParser();
        long adStartTime = vastResponse.getLong("time") / 1000; // Start time for ads (in seconds).
        VastAdEntrySet set = parser.parseEntries(vastResponse);
        VastAdEntrySet setCopy = parser.parseEntries(vastResponse);

        Assert.assertTrue(set.equals(setCopy));

        // First batch should contain 6 entries.
        Assert.assertEquals(6, set.getEntries().size());

        // Start times for ads (in seconds). Corresponds to the values in the VAST document.
        long    firstAd = adStartTime,
                secondAd = firstAd + 120,
                thirdAd = secondAd + 5,
                forthAd = thirdAd + 60,
                fifthAd = forthAd + 10,
                sixthAd = fifthAd + 20;
        Assert.assertNull(set.getEntry(0)); // Before the first ad.

        Assert.assertNotEquals(set.getEntry(firstAd * 1000), set.getEntry(secondAd * 1000 + 1));
        Assert.assertEquals(set.getEntry(thirdAd * 1000), set.getEntry(thirdAd * 1000));

        Assert.assertEquals(AdEventType.Start, set.getEntry(firstAd * 1000).getImpression().type);
        // Sending the first ad by setting "sent" to true
        Assert.assertFalse(set.getEntry(firstAd * 1000).getImpression().isSent());
        set.getEntry(firstAd * 1000).getImpression().setSent();
        Assert.assertTrue(set.getEntry(firstAd * 1000).getImpression().isSent());

        // Iterate over the first entry and check that all positions on the timeline refers to this entry
        VastAdEntry entry = set.getEntry(1000 * firstAd);
        Assert.assertEquals(AdEventType.Start, entry.getImpression().type);
        for(long i = adStartTime * 1000; i < 121 * 1000; i += 100) {
            Assert.assertEquals(entry, set.getEntry(i));

        }
        // Check the positions of the first (120 sec) ad.
        Assert.assertEquals(AdEventType.FirstQuartile, set.getEntry(1000 * (firstAd + 31)).getImpression().type);
        Assert.assertEquals(AdEventType.MidPoint, set.getEntry(1000 * (firstAd + 61)).getImpression().type);
        Assert.assertEquals(AdEventType.ThirdQuartile, set.getEntry(1000 * (firstAd + 91)).getImpression().type);
        Assert.assertEquals(AdEventType.Complete, set.getEntry(1000 * (firstAd + 119)).getImpression().type);

        // Check the positions of the first 5 sec ad.
        Assert.assertEquals(AdEventType.Start, set.getEntry(1000 * secondAd + 1).getImpression().type);
        Assert.assertEquals(AdEventType.FirstQuartile, set.getEntry(1000 * (secondAd) + 5000 / 4 + 1).getImpression().type);
        Assert.assertEquals(AdEventType.MidPoint, set.getEntry(1000 * (secondAd) + 5000 / 2 + 1).getImpression().type);
        Assert.assertEquals(AdEventType.ThirdQuartile, set.getEntry(1000 * (secondAd) + ((long)(5000.0 * 0.75)) + 1).getImpression().type);
        Assert.assertEquals(AdEventType.Complete, set.getEntry(1000 * (secondAd) + 5000).getImpression().type);

        // Third ad should not have a Start event.
        Assert.assertNull(set.getEntry(1000 * (thirdAd + 1)).getImpression());
        // Third ad should have a FirstQuartile event.
        Assert.assertEquals(AdEventType.FirstQuartile, set.getEntry(1000 * (thirdAd + 16)).getImpression().type); // 16 ~ 75 % of 20 seconds.

        // If setting `sent` to true, this event region will not return any entries.
        set.getEntry(1000 * (thirdAd + 16)).getImpression().setSent();
        Assert.assertTrue(set.getEntry(1000 * (thirdAd + 16)).getImpression().isSent());
        set = setCopy; // Replace the set with a fresh copy and it should return entries.
        Assert.assertFalse(set.getEntry(1000 * (thirdAd + 16)).getImpression().isSent());

        Assert.assertEquals(AdEventType.MidPoint, set.getEntry(     1000 * (thirdAd + 31)).getImpression().type);
        Assert.assertEquals(AdEventType.ThirdQuartile, set.getEntry(1000 * (thirdAd + 46)).getImpression().type);
        Assert.assertEquals(AdEventType.Complete, set.getEntry(1000 * (thirdAd + 59)).getImpression().type);
        Assert.assertNull(set.getEntry(1000 *(sixthAd + 15 + 1))); // After the last ad.

        // Get the URLS: from start to complete of the first ad entry.
        Assert.assertEquals(1, set.getEntry(1000 * (1 + firstAd)).getImpression().getUrls().size());
        Assert.assertEquals(3, set.getEntry(1000 * (31 + firstAd)).getImpression().getUrls().size());
        Assert.assertEquals(3, set.getEntry(1000 * (61 + firstAd)).getImpression().getUrls().size());
        Assert.assertEquals(3, set.getEntry(1000 * (91 + firstAd)).getImpression().getUrls().size());
        Assert.assertEquals(4, set.getEntry(1000 * (119 + firstAd)).getImpression().getUrls().size());
    }

    @Test
    public void testAdsFactory() throws IOException, JSONException {
        ITimeline timeline = new SimpleTimeline();
        MockHttpHandler mockHttpHandler = new MockHttpHandler();

        JSONObject playResponse = resourceLoader.loadJSON("ssai.hls.live.play.response.json");
        JSONObject mediaFormat = playResponse.getJSONArray("formats").getJSONObject(0);
        IAdMetadata adsnfo = new ExposureAdMetadata(playResponse.getJSONObject("ads"), EnigmaMediaFormat.parseMediaFormat(mediaFormat).getStreamFormat(), true );
        IAdDetector detector = new AdDetector(mockHttpHandler, timeline, timeFactory);
        IAdResourceLoader resourceLoader = detector.getFactory().createResourceLoader(adsnfo, "https://example.com");
        Assert.assertNotNull(resourceLoader);
        Assert.assertEquals(resourceLoader.getClass(), NowtilusHlsLiveResourceLoader.class);

        VastAdEntry entry1 = new VastAdEntry("id1", "title1", 0, 1000, new HashMap<>(), null);
        VastAdEntry entry2 = new VastAdEntry("id2", "title2", 1000, 2000, new HashMap<>(), null);
        Assert.assertNotEquals(entry1, entry2);
        Assert.assertEquals(entry1, entry1);
    }

    /** Test playback of a stream by continuously iterating over the timeline. This mainly test the delegate callback. */
    @Test public void testNowtilusHLSLiveDetectorContinousPlayback() throws IOException, JSONException {

        MockAdsTestHelpers.initializeLiveAdDetector(this);

        mockHttpHandler.queueResponseOk(Pattern.compile(".*"), resourceLoader.loadString("ssai.hls.vast.live.response.json"));
        adsDetector.update(hlsLiveResourceLoader, manifestTime);
        Assert.assertEquals(0, listener.changeCount);
        Assert.assertFalse(listener.adsPlaying);
        Assert.assertNull(listener.latestAd);

        int adCount = adsDetector.getLatestAds().getEntries().size();

        // It should contain 6 ads
        Assert.assertEquals(6, adCount);

        // Step over the 6 ads by moving the timeline steps of 10 ms.
        for (int position = 0; position < 400 * 1000; position += 10) { timeline.setCurrentPosition(timeFactory.newPosition(position)); }

        // Delegate should have been called 5 times for each ad + 1 for the final `null` entry once closed
        Assert.assertEquals(adCount * 5 + 1, listener.changeCount);

        // Step over the 6 ads by moving the timeline steps of 10 ms.
        for (int position = 0; position < 400 * 1000; position += 10) { timeline.setCurrentPosition(timeFactory.newPosition(position)); }

        // No changes in delegate count let alone an event caused by the reset of the ads, since events should have been sent already
        Assert.assertEquals(adCount * 5 + 2, listener.changeCount);

        listener.changeCount = 0; // reset change tracker

        adsDetector.update(hlsLiveResourceLoader, manifestTime);

        // Step over the 6 ads by moving the timeline steps of 20 ms.
        for (int position = 0; position < 400 * 1000; position += Math.random() * 50) { timeline.setCurrentPosition(timeFactory.newPosition(position)); }

        // No changes in delegate count, since the ads list haven't been updated with NEW ads except the last "null" event
        Assert.assertEquals(1, listener.changeCount);

        listener.changeCount = 0; // reset change tracker

        // Add a new list of ads
        mockHttpHandler.queueResponseOk(Pattern.compile(".*"), resourceLoader.loadString("ssai.hls.vast.live.response.2.json"));
        adsDetector.update(hlsLiveResourceLoader, manifestTime);

        adCount = adsDetector.getLatestAds().getEntries().size();

        // No changes in delegate count, since the ads list haven't been updated
        Assert.assertEquals(0, listener.changeCount);

        // Step over the 6 ads by moving the timeline steps of 20 ms.
        for (int position = 0; position < 400 * 1000; position += Math.random() * 50) { timeline.setCurrentPosition(timeFactory.newPosition(position)); }

        // Delegate should have been called 5 times for each ad + 1 for the final `null` entry once closed
        Assert.assertEquals(adCount * 5 + 1, listener.changeCount);
    }
    /**
     * Test VastAdsDetector by loading a manifest and mocking endpoints.
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testNowtilusHLSLiveDetector() throws IOException, JSONException {

        MockAdsTestHelpers.initializeLiveAdDetector(this);

        /* Start test */

        // First response is a HTTP 204 (no ad information).
        mockHttpHandler.queueResponse(new HttpStatus(204, null));
        adsDetector.update(hlsLiveResourceLoader, manifestTime);
        Assert.assertEquals(0, listener.changeCount);
        Assert.assertFalse(listener.adsPlaying);

        // Second response contains ad information, but the ads starts at `manifestTime`.

        mockHttpHandler.queueResponseOk(Pattern.compile(".*"), resourceLoader.loadString("ssai.hls.vast.live.response.json"));
        adsDetector.update(hlsLiveResourceLoader, manifestTime);
        Assert.assertEquals(0, listener.changeCount);
        Assert.assertFalse(listener.adsPlaying);
        Assert.assertNull(listener.latestAd);
        int requestsSent = mockHttpHandler.getLog().size();

        // Move to the time of the first ad (120 sec).
        playerTime = adStartTime + 1;
        timeline.setCurrentPosition(timeFactory.newPosition(playerTime));

        // First batch should contain 6 entries.
        Assert.assertEquals(6, adsDetector.getLatestAds().getEntries().size());

        // Delegate should not have been called
        Assert.assertEquals(1, listener.changeCount);
        Assert.assertTrue(listener.adsPlaying);
        Assert.assertNotNull(listener.latestAd);
        Assert.assertEquals(120 * 1000, listener.latestAd.getDuration());
        Assert.assertEquals(requestsSent + 1, mockHttpHandler.getLog().size()); // Sent one "Start" event.
        Assert.assertEquals("https://clientside-tracking.ssai.prod.serverside.ai/5207e4b5-7f1b-42f8-b2e3-0653839223be/8d974880-28ac-11eb-962d-3481ecd10445/74c8231f-ec21-435b-8d3d-c14e2b5f8368/817a6e3d-de0d-4f4c-908f-f3f6d86dcbc1/start",
                new JSONObject(mockHttpHandler.getLog().get(requestsSent)).getString("url")); // Start event emits 1 network call for first ad.
        IAd latestAd = listener.latestAd; // Store a reference to the last ad.


        // Repeat the request
        mockHttpHandler.queueResponseOk(Pattern.compile(".*"), resourceLoader.loadString("ssai.hls.vast.live.response.json"));
        adsDetector.update(hlsLiveResourceLoader, manifestTime);
        // Delegate should not have been called.
        Assert.assertEquals(1, listener.changeCount);
        Assert.assertTrue(listener.adsPlaying);
        Assert.assertEquals(latestAd, listener.latestAd); // The ad should have been changed.
        requestsSent = mockHttpHandler.getLog().size();

        // "Scrub" to the start of the next ad (5 sec).
        playerTime += 120 * 1000 + 1;
        timeline.setCurrentPosition(timeFactory.newPosition(playerTime));
        // Delegate should have been called
        Assert.assertEquals(2, listener.changeCount);
        Assert.assertTrue(listener.adsPlaying);
        Assert.assertNotNull(listener.latestAd);
        Assert.assertEquals(5 * 1000, listener.latestAd.getDuration());
        Assert.assertNotEquals(latestAd, listener.latestAd); // It should be a new ad.
        Assert.assertEquals(requestsSent + 1, mockHttpHandler.getLog().size()); // Sent one "Start" event.
        Assert.assertEquals("https://clientside-tracking.ssai.prod.serverside.ai/5207e4b5-7f1b-42f8-b2e3-0653839223be/8d974880-28ac-11eb-962d-3481ecd10445/74c8231f-ec21-435b-8d3d-c14e2b5f8368/0b49b589-008b-4955-9693-1ac474b4c6bb/start",
                new JSONObject(mockHttpHandler.getLog().get(requestsSent)).getString("url")); // Start event emits 1 network call for second ad.
        latestAd = listener.latestAd;
        requestsSent = mockHttpHandler.getLog().size();

        // "Scrub" to a location where the third ad will have started playing.
        playerTime += 5 * 1000 + 1;
        timeline.setCurrentPosition(timeFactory.newPosition(playerTime));
        // Delegate should have been called
        Assert.assertEquals(3, listener.changeCount);
        Assert.assertTrue(listener.adsPlaying);
        Assert.assertNotEquals(latestAd, listener.latestAd); // It should be a new ad
        Assert.assertEquals(requestsSent, mockHttpHandler.getLog().size()); // There should have been no reporting call, since "Start" event is missing from the third ad.

        // Now, the ads should have started in the past, since the manifest states that "current time" is in the far future
        manifestTime = 3600 * 1000;
        mockHttpHandler.queueResponseOk(Pattern.compile(".*"), resourceLoader.loadString("ssai.hls.vast.live.response.json"));
        adsDetector.update(hlsLiveResourceLoader, manifestTime);
        playerTime += 1; // Step only one ms into the future in order to call the adsUpdater.
        timeline.setCurrentPosition(timeFactory.newPosition(playerTime));
        // Listener should have been alerted and no active ad should be playing.
        Assert.assertEquals(4, listener.changeCount);
        Assert.assertNull(listener.latestAd);
        Assert.assertFalse(listener.adsPlaying);
    }
}
