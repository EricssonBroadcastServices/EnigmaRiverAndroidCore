// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.video;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.player.EnigmaPlayer;
import com.redbeemedia.enigma.core.player.ITimelinePositionFactory;
import com.redbeemedia.enigma.core.player.MockPlayerImplementation;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.time.Duration;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class SpriteDataTests {

    private final ITimelinePositionFactory timelinePositionFactory;

    public SpriteDataTests() {
        // These three lines are idiosyncratically needed in order to retrieve the `TimeLinePositionFactory`... sigh...
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        MockPlayerImplementation mockPlayerImplementation = new MockPlayerImplementation();
        new EnigmaPlayer(new MockSession(), mockPlayerImplementation);
        timelinePositionFactory = mockPlayerImplementation.timelinePositionFactory;
    }

    private final String vttString = "WEBVTT\n" +
            "\n" +
            "00:00:00.000 --> 00:00:10.000\n" +
            "sprites.jpg#xywh=0,0,160,90\n" +
            "\n" +
            "00:00:10.000 --> 00:00:20.000\n" +
            "sprites.jpg#xywh=160,0,160,90\n" +
            "\n" +
            "00:00:20.000 --> 00:00:30.000\n" +
            "sprites.jpg#xywh=320,0,160,90\n" +
            "\n" +
            "00:00:30.000 --> 00:00:40.000\n" +
            "sprites2.jpg#xywh=480,0,160,90\n" +
            "\n" +
            "00:00:40.000 --> 00:00:50.000\n" +
            "sprites2.jpg#xywh=640,0,160,90\n" +
            "\n" +
            "00:00:50.000 --> 00:01:00.000\n" +
            "/absolute/path/to/sprites3.jpg#xywh=0,90,160,90"+
            "\n" +
            "00:01:00.000 --> 00:01:10.000\n" +
            "sprites.jpg#xywh=160,90,160,90\n" +
            "\n" +
            "00:01:10.000 --> 00:01:20.000\n" +
            "sprites.jpg#xywh=320,90,160,90\n" +
            "\n" +
            "00:01:20.000 --> 00:01:30.000\n" +
            "sprites.jpg#xywh=480,90,160,90\n" +
            "\n" +
            "00:01:30.000 --> 00:01:40.000\n" +
            "sprites.jpg#xywh=640,90,160,90\n" +
            "\n" +
            "00:01:40.000 --> 00:01:50.000\n" +
            "sprites.jpg#xywh=0,180,160,90\n" +
            "\n" +
            "00:01:50.000 --> 00:02:00.000\n" +
            "sprites.jpg#xywh=160,180,160,90\n" +
            "\n" +
            "00:02:00.000 --> 00:02:10.000\n" +
            "sprites.jpg#xywh=320,180,160,90\n" +
            "\n" +
            "00:02:10.000 --> 00:02:20.000\n" +
            "sprites.jpg#xywh=480,180,160,90\n" +
            "\n" +
            "00:02:20.000 --> 00:02:30.000\n" +
            "sprites.jpg#xywh=640,180,160,90\n" +
            "\n" +
            "00:02:30.000 --> 00:02:40.000\n" +
            "sprites.jpg#xywh=0,270,160,90\n" +
            "\n" +
            "00:02:40.000 --> 00:02:50.000\n" +
            "sprites.jpg#xywh=160,270,160,90\n" +
            "\n" +
            "00:02:50.000 --> 00:03:00.000\n" +
            "sprites.jpg#xywh=320,270,160,90\n" +
            "\n" +
            "01:00:00.500 --> 01:00:00.750\n" +
            "sprites3.jpg#xywh=0,90,160,90";

    private final String vttString2 = "WEBVTT\n" +
            "\n" +
            "01:00:00.000 --> 01:30:00.000\n" +
            "sprites1.jpg#xywh=0,0,160,90\n" +
            "\n" +
            "02:00:10.000 --> 02:00:20.000\n" +
            "sprites2.jpg#xywh=160,0,160,90\n";

    private final String vttString3 = "WEBVTT\n" +
            "\n" +
            "01:00:00.000 --> 01:30:00.000\n" +
            "sprites.jpg#xywh=0,0,160,90\n" +
            "\n" +
            "02:00:10.000 --> 02:00:20.000\n" +
            "https://www.example.com/sprites2.jpg#xywh=160,0,160,90\n" +
            "\n" +
            "02:00:10.000 --> 02:00:20.000\n" +
            "https://www.example.com/sprites2.jpg?query=42&other_query=yes%3D+no#xywh=160,0,160,90\n";

    @Test
    public void testVideSpriteVttParser() throws MalformedURLException {

        // Create parser and parse the string
        SpriteDataVttParser parser = new SpriteDataVttParser(timelinePositionFactory);
        ArrayList<SpriteData> sprites = new ArrayList(parser.parse(new URL("https://example.com/sprites/sprites.vtt"), vttString));

        Assert.assertEquals(19, sprites.size());

        // Check that the image urls are correct
        Assert.assertEquals(new URL("https://example.com/sprites/sprites.jpg"), sprites.get(0).imageUrl);
        Assert.assertEquals(new URL("https://example.com/sprites/sprites2.jpg"), sprites.get(3).imageUrl);
        Assert.assertEquals(new URL("https://example.com/absolute/path/to/sprites3.jpg"), sprites.get(5).imageUrl);

        // The first 18 sprites are sequential and copied from a "real" response.
        for (int i = 0 ; i < 18; i++) {

            // The first 18 sprites should be sequential and have a 10 s duration
            Assert.assertEquals(10 * 1000, sprites.get(i).duration.inWholeUnits(Duration.Unit.MILLISECONDS));

            if (i > 0) { Assert.assertTrue(sprites.get(i - 1).position.before(sprites.get(i).position)); }

            // Frame order should be sequential with a width of 160 and height of 90.
            Assert.assertEquals(new SpriteData.Frame((i % 5) * 160, (i / 5) * 90, 160 ,90), sprites.get(i).frame);

            // Check the position (it's 10 s interval for the first 18)
            Assert.assertEquals(timelinePositionFactory.newPosition(i * 10 * 1000), sprites.get(i).position);

        }

        // The last sprite should start an hour and 500 ms in the future and be 250 ms long.
        Assert.assertEquals(250, sprites.get(18).duration.inWholeUnits(Duration.Unit.MILLISECONDS));
        Assert.assertEquals(timelinePositionFactory.newPosition(60 * 60 * 1000 + 500), sprites.get(18).position);
    }

    @Test
    public void testSpriteDataRepository() throws TimeoutException, IOException, InterruptedException {

        MockHttpHandler httpHandler = new MockHttpHandler();

        // Configures the sprite repository to keep the last response and to return an image
        httpHandler.keepLastResponse = true;
        httpHandler.queueBinaryResponseOk(Pattern.compile(".*\\.jpg"), SpriteDataMock.createSerializedMockImage(20, 10));

        SpriteRepository spriteRepo = new SpriteRepository(timelinePositionFactory, httpHandler);
        spriteRepo.setImageRepository(new SpriteDataMock.MockImageRepository(httpHandler));

        // Not yet configured (no base urls).
        spriteRepo.activate(161, sprites -> Assert.assertTrue(sprites.isEmpty()));

        // Set up the available sprite widths
        HashMap<Integer, String> positions = new HashMap<>();
        positions.put(160, "https://example.com/sprites/sprites.vtt");
        positions.put(320, "https://example.com/sprites/sprites2.vtt");
        spriteRepo.setVTTUrls(positions, new MockSession());

        // No sprites with width 161
        httpHandler.queueResponseOk(Pattern.compile(".*\\.vtt"), vttString); // Http server will return the vtt string
        spriteRepo.activate(161, null);
        SpriteDataMock.waitForSprites(spriteRepo, 1000);
        spriteRepo.activate(161, sprites -> Assert.assertTrue(sprites.isEmpty()));
        SpriteDataMock.waitForSprites(spriteRepo, 1000);

        // Retrieve the list
        spriteRepo.activate(160, null);
        SpriteDataMock.waitForSprites(spriteRepo, 1000);

        spriteRepo.activate(160, sprites -> Assert.assertEquals(19, sprites.size()));
        SpriteDataMock.waitForSprites(spriteRepo, 1000);

        ArrayList<SpriteData> sprites = new ArrayList<>(spriteRepo.getSprites());

        // Test  getSprite(ITimelinePosition)
        for(int i = 0; i < 18; i++) {
            // Before
            Assert.assertNotEquals(sprites.get(i), spriteRepo.getSpriteData(timelinePositionFactory.newPosition((i - 1) * 10 * 1000 - i * (10000 / 18) - 1)));
            // At time
            Assert.assertEquals(sprites.get(i), spriteRepo.getSpriteData(timelinePositionFactory.newPosition(i * 10 * 1000 + i * (10000 / 18))));
            // After
            Assert.assertNotEquals(sprites.get(i), spriteRepo.getSpriteData(timelinePositionFactory.newPosition((i + 1) * 10 * 1000 + (i) * (10000 / 18))));
        }

        // Test the fringes of the last sprite
        Assert.assertNotEquals(sprites.get(18), spriteRepo.getSpriteData(timelinePositionFactory.newPosition(60 * 60 * 1000 + 499)));
        Assert.assertEquals(sprites.get(18), spriteRepo.getSpriteData(timelinePositionFactory.newPosition(60 * 60 * 1000 + 500)));
        Assert.assertEquals(sprites.get(18), spriteRepo.getSpriteData(timelinePositionFactory.newPosition(60 * 60 * 1000 + 749)));
        Assert.assertNotEquals(sprites.get(18), spriteRepo.getSpriteData(timelinePositionFactory.newPosition(60 * 60 * 1000 + 750)));

        // Test simple sprite retrieval.
        httpHandler.keepLastResponse = false; // Make headway for a new VTT request after the one below
        spriteRepo.activate(s -> Assert.assertEquals(19, s.size()));
        SpriteDataMock.waitForSprites(spriteRepo, 1000);
        Thread.sleep(200); // Wait for the images to be "fetched"

        sprites = new ArrayList<>(spriteRepo.getSprites());

        // Make sure the image URLs are created correctly
        Assert.assertEquals(new URL("https://example.com/sprites/sprites.jpg"), sprites.get(0).imageUrl);
        Assert.assertEquals(new URL("https://example.com/sprites/sprites2.jpg"), sprites.get(3).imageUrl);
        Assert.assertEquals(new URL("https://example.com/absolute/path/to/sprites3.jpg"), sprites.get(5).imageUrl);

        // Retrieve the list with 2 elements
        httpHandler.queueResponseOk(Pattern.compile(".*\\.vtt"), vttString2);  // Will respond with the content of string `vttString2`.
        spriteRepo.activate(320, s -> Assert.assertEquals(2, s.size()));
        SpriteDataMock.waitForSprites(spriteRepo, 1000);

        sprites = new ArrayList<>(spriteRepo.getSprites());

        // Check the position
        Assert.assertEquals(timelinePositionFactory.newPosition(60 * 60 * 1000), sprites.get(0).position);
        Assert.assertEquals(timelinePositionFactory.newPosition(2 * 60 * 60 * 1000 + 10 * 1000), sprites.get(1).position);

        // Check the duration
        Assert.assertEquals(30 * 60 * 1000, sprites.get(0).duration.inWholeUnits(Duration.Unit.MILLISECONDS));
        Assert.assertEquals(10 * 1000, sprites.get(1).duration.inWholeUnits(Duration.Unit.MILLISECONDS));

        // Check frames
        Assert.assertEquals(new SpriteData.Frame(0,0,160,90), sprites.get(0).frame);
        Assert.assertEquals(new SpriteData.Frame(160,0,160,90), sprites.get(1).frame);

        // Test release
        spriteRepo.clear();
        Assert.assertTrue(spriteRepo.getSprites().isEmpty());

        // Test that the repo can be updated
        httpHandler.queueResponseOk(Pattern.compile(".*\\.vtt"), vttString2);  // Will respond with the content of string `vttString2`.
        spriteRepo.activate(320, s -> Assert.assertEquals(2, s.size()));
        SpriteDataMock.waitForSprites(spriteRepo, 1000);
        Assert.assertFalse(spriteRepo.getSprites().isEmpty());

        // Test the parsing of sprites with images that comes as URLs
        httpHandler.queueResponseOk(Pattern.compile(".*\\.vtt"), vttString3);  // Will respond with the content of string `vttString2`.
        spriteRepo.activate(s -> Assert.assertEquals(3, s.size()));
        SpriteDataMock.waitForSprites(spriteRepo, 1000);
        sprites = new ArrayList<>(spriteRepo.getSprites());
        Assert.assertEquals(sprites.get(0).imageUrl, new URL("https://example.com/sprites/sprites.jpg"));
        Assert.assertEquals(sprites.get(1).imageUrl, new URL("https://www.example.com/sprites2.jpg"));
        Assert.assertEquals(sprites.get(2).imageUrl, new URL("https://www.example.com/sprites2.jpg?query=42&other_query=yes%3D+no"));
    }

    @Test
    public void testImageRepository() throws MalformedURLException, IOException {
        MockHttpHandler httpHandler = new MockHttpHandler();
        SpriteDataMock.MockImageRepository repository = new SpriteDataMock.MockImageRepository(httpHandler);

        // Set up the frames for the container images
        SpriteData.Frame frame1 = new SpriteData.Frame(0, 0, 10, 10);
        SpriteData.Frame frame2 = new SpriteData.Frame(10, 0, 10, 10);
        SpriteData.Frame frame3 = new SpriteData.Frame(0, 0, 8, 9);
        SpriteData.Frame frame4 = new SpriteData.Frame(10, 20, 12, 13);

        // Create metadata for the container images
        SpriteData sprite1 = new SpriteData(Duration.millis(10), timelinePositionFactory.newPosition(10), new URL("https://example.com/sprite1.jpg"), frame1);
        SpriteData sprite2 = new SpriteData(Duration.millis(10), timelinePositionFactory.newPosition(10), new URL("https://example.com/sprite1.jpg"), frame2);
        SpriteData sprite3 = new SpriteData(Duration.millis(10), timelinePositionFactory.newPosition(10), new URL("https://example.com/sprite2.jpg"), frame3);
        SpriteData sprite4 = new SpriteData(Duration.millis(10), timelinePositionFactory.newPosition(10), new URL("https://example.com/sprite3.jpg"), frame4);
        ArrayList<SpriteData> sprites = new ArrayList<>();
        sprites.add(sprite1); sprites.add(sprite2); sprites.add(sprite3); sprites.add(sprite4);

        // Test failed fetch
        httpHandler.queueResponse(new HttpStatus(404, "image not found"));
        repository.cacheImages(sprites, count -> Assert.assertEquals(0, count));
        SpriteDataMock.sleep100();

        // Test successful fetch by creating a mocked master image"
        httpHandler.queueBinaryResponseOk(Pattern.compile("https://example.com/sprite1.jpg"), SpriteDataMock.createSerializedMockImage(20, 10)); // images for first URL
        httpHandler.queueBinaryResponseOk(Pattern.compile("https://example.com/sprite2.jpg"), SpriteDataMock.createSerializedMockImage(20, 10)); // images for second URL
        httpHandler.queueBinaryResponseOk(Pattern.compile("https://example.com/sprite3.jpg"), SpriteDataMock.createSerializedMockImage(100, 100)); // images for third URL

        repository.cacheImages(sprites, count -> Assert.assertEquals(3, count));
        SpriteDataMock.sleep100();

        // Test fetch of the first sprite image
        SpriteDataMock.MockImage image1 = repository.getImage(sprite1);
        Assert.assertEquals(sprite1.frame.width, image1.width);
        Assert.assertEquals(sprite1.frame.height, image1.height);

        // Test fetch of the second sprite image
        SpriteDataMock.MockImage image2 = repository.getImage(sprite2);
        Assert.assertEquals(sprite2.frame.width, image2.width);
        Assert.assertEquals(sprite2.frame.height, image2.height);

        // Test fetching of images that should not exist
        SpriteData spriteWithWrongUrl = new SpriteData(Duration.millis(10), timelinePositionFactory.newPosition(10), new URL("https://example.com/sprite4.jpg"), frame2);
        SpriteData spriteWithWrongFrame = new SpriteData(Duration.millis(10), timelinePositionFactory.newPosition(10), new URL("https://example.com/sprite.jpg"), SpriteData.Frame.zero);
        Assert.assertNull(repository.getImage(spriteWithWrongUrl));
        Assert.assertNull(repository.getImage(spriteWithWrongFrame));

        // Test partial failures of fetching container images
        // Test successful fetch by creating a mocked master image"
        httpHandler.queueBinaryResponseOk(Pattern.compile("https://example.com/sprite1.jpg"), SpriteDataMock.createSerializedMockImage(20, 10)); // images for first URL
        httpHandler.queueResponse(new HttpStatus(404, "image not found"));
        httpHandler.queueBinaryResponseOk(Pattern.compile("https://example.com/sprite3.jpg"), SpriteDataMock.createSerializedMockImage(100, 100)); // images for third URL
        repository.cacheImages(sprites, count -> Assert.assertEquals(2, count));
        SpriteDataMock.sleep100();

        // Sprite for sprite4 should have been loaded.
        SpriteDataMock.MockImage image4 = repository.getImage(sprite4);
        Assert.assertEquals(sprite4.frame.width, image4.width);
        Assert.assertEquals(sprite4.frame.height, image4.height);

        // Test release
        repository.clear();
        Assert.assertNull(repository.getImage(sprite1));
        Assert.assertNull(repository.getImage(sprite2));
        Assert.assertNull(repository.getImage(sprite3));
        Assert.assertNull(repository.getImage(sprite4));
    }
}
