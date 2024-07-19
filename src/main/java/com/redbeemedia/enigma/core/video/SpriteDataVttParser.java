// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.video;

import android.util.Log;

import com.redbeemedia.enigma.core.player.ITimelinePositionFactory;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parsing a string containing sprites VTT data. */
class SpriteDataVttParser {
    private static final String TAG = SpriteDataVttParser.class.getName();
    private final long TIME_NOT_FOUND = -1;
    private static final String WEBVTTTAG = "WEBVTT";

    private final ITimelinePositionFactory timelinePositionFactory;
    private final Pattern timePattern = Pattern.compile("[\\d]{2}:[\\d]{2}:[\\d]{2}\\.[\\d]{3}");
    private final Pattern imageSourcePattern = Pattern.compile("^.+#");
    private final Pattern framePattern = Pattern.compile("xywh=[0-9]+,[0-9]+,[0-9]+,[0-9]+");

    SpriteDataVttParser(ITimelinePositionFactory timelinePositionFactory) {
        this.timelinePositionFactory = timelinePositionFactory;
    }

    ITimelinePosition createTimelinePosition(long milliseconds) {
        return timelinePositionFactory.newPosition(milliseconds);
    }

    /**
     * Tries to parse a string containing vtt information.
     * @param vttUrl contains the URL to the VTT file.
     * @param vtt a String containing the vtt information
     * @return a list of SpriteData models.
     */
    Collection<SpriteData> parse(URL vttUrl, String vtt) {

        List<SpriteData> sprites = new ArrayList<>();

        Scanner scanner = new Scanner(vtt);
        String line = scanner.nextLine();
        if (!line.toUpperCase().equals(WEBVTTTAG)) {
            Log.d(TAG, "Sprite is missing " + WEBVTTTAG + " start entry.");
        }
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if(line.trim().isEmpty()) { continue; }

            Matcher matcher = timePattern.matcher(line);
            long startTime = TIME_NOT_FOUND, endTime = TIME_NOT_FOUND;
            URL url = null;
            SpriteData.Frame frame = SpriteData.Frame.zero;

            if (matcher.find()) { startTime = parseTime(matcher.group()); }
            if (matcher.find()) { endTime = parseTime(matcher.group()); }

            line = scanner.nextLine();
            matcher = imageSourcePattern.matcher(line);
            if (matcher.find()) { url = parseUrl(vttUrl, matcher.group().trim().replace("#", "")); }
            matcher = framePattern.matcher(line);
            if (matcher.find()) { frame = parseFrame(matcher.group()); }

            if (startTime == TIME_NOT_FOUND || endTime == TIME_NOT_FOUND || url == null || frame == SpriteData.Frame.zero) {
                Log.w(TAG, "Unable to parse a sprite model from VTT: '" + vttUrl + "'");
                continue;
            }
           sprites.add(new SpriteData(
                    Duration.millis(endTime - startTime),
                    timelinePositionFactory.newPosition(startTime),
                    url,
                    frame)
            );

        }
        scanner.close();
        return sprites;
    }

    private long parseTime(String timeString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format.parse(timeString).getTime();
        } catch (ParseException e) { e.printStackTrace(); }
        return TIME_NOT_FOUND;
    }

    private SpriteData.Frame parseFrame(String frameString) {
        Matcher matcher = Pattern.compile("[0-9]+").matcher(frameString);
        int[] rect = new int[4];
        for (int i = 0; i < 4; i++) {
            if (matcher.find()) {
                rect[i] = Integer.parseInt(matcher.group());
            }
        }
        return new SpriteData.Frame(rect[0], rect[1], rect[2], rect[3]);
    }

    private String stripFilename(URL url) {
        String stringUrl = url.toString();
        return stringUrl.substring(0, stringUrl.lastIndexOf('/'));
    }

    private URL parseUrl(URL vttUrl, String filename) {
        try {
            try  { return new URL(filename); }
            catch (Exception ignored) {}
            if (filename.startsWith("/")) {
                return new URL(vttUrl.getProtocol(), vttUrl.getHost(), vttUrl.getPort(), filename);
            } else {
                return new UrlPath(stripFilename(vttUrl)).append(filename).toURL();
            }
        } catch (MalformedURLException e) { e.printStackTrace(); }
        return null;
    }
}
