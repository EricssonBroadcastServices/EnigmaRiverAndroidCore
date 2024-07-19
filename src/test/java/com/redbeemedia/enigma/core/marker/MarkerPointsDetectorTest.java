// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.marker;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.redbeemedia.enigma.core.player.timeline.EnigmaMetadata;
import com.redbeemedia.enigma.core.player.timeline.EnigmaHlsMediaPlaylist;
import com.redbeemedia.enigma.core.player.DefaultTimelinePositionFactoryTest;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.timeline.ITimelineListener;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class MarkerPointsDetectorTest {

    private MarkerPointsDetector detector;
    DefaultTimelinePositionFactoryTest.TimeLinePositionCreator timeFactory = new DefaultTimelinePositionFactoryTest.TimeLinePositionCreator();


    @Test
    public void getLastMarkerPoint() {
        detector = createDetector(11000);
        MarkerPoint markerPoint1 = new MarkerPoint(MarkerType.INTRO, 10000, 1000, new ArrayList<>());
        MarkerPoint markerPoint2 = new MarkerPoint(MarkerType.CHAPTER, 0, 20000, new ArrayList<>());
        detector.markerPointList = new ArrayList<>();
        detector.markerPointList.add(markerPoint1);
        detector.markerPointList.add(markerPoint2);


        MarkerPoint lastMarkerPoint = detector.getLastMarkerPoint();
        Assert.assertEquals( markerPoint1, lastMarkerPoint);
    }

    @Test
    public void getLastMarkerPoint2() {
        detector = createDetector(0);
        MarkerPoint markerPoint1 = new MarkerPoint(MarkerType.INTRO, 10000, 1000, new ArrayList<>());
        MarkerPoint markerPoint2 = new MarkerPoint(MarkerType.CHAPTER, 30000, 20000, new ArrayList<>());
        detector.markerPointList = new ArrayList<>();
        detector.markerPointList.add(markerPoint1);
        detector.markerPointList.add(markerPoint2);


        MarkerPoint lastMarkerPoint = detector.getLastMarkerPoint();
        Assert.assertNull(lastMarkerPoint);
    }

    @Test
    public void getLastMarkerPoint3() {
        detector = createDetector(1000);
        MarkerPoint markerPoint1 = new MarkerPoint(MarkerType.INTRO, 10000, 1000, new ArrayList<>());
        MarkerPoint markerPoint2 = new MarkerPoint(MarkerType.CHAPTER, 30000, 20000, new ArrayList<>());
        detector.markerPointList = new ArrayList<>();
        detector.markerPointList.add(markerPoint1);
        detector.markerPointList.add(markerPoint2);

        MarkerPoint lastMarkerPoint = detector.getLastMarkerPoint();
        Assert.assertEquals( markerPoint1, lastMarkerPoint);
    }

    @Test
    public void getLastMarkerPoint3_1() {
        detector = createDetector(1001);
        MarkerPoint markerPoint1 = new MarkerPoint(MarkerType.INTRO, 10000, 1000, new ArrayList<>());
        MarkerPoint markerPoint2 = new MarkerPoint(MarkerType.CHAPTER, 30000, 20000, new ArrayList<>());
        detector.markerPointList = new ArrayList<>();
        detector.markerPointList.add(markerPoint1);
        detector.markerPointList.add(markerPoint2);

        MarkerPoint lastMarkerPoint = detector.getLastMarkerPoint();
        Assert.assertEquals( markerPoint1, lastMarkerPoint);
    }

    @Test
    public void getLastMarkerPoint4() {
        detector = createDetector(40000);
        MarkerPoint markerPoint1 = new MarkerPoint(MarkerType.INTRO, 10000, 1000, new ArrayList<>());
        MarkerPoint markerPoint2 = new MarkerPoint(MarkerType.CHAPTER, 30000, 20000, new ArrayList<>());
        detector.markerPointList = new ArrayList<>();
        detector.markerPointList.add(markerPoint1);
        detector.markerPointList.add(markerPoint2);

        MarkerPoint lastMarkerPoint = detector.getLastMarkerPoint();
        Assert.assertEquals( markerPoint2, lastMarkerPoint);
    }

    @Test
    public void getLastMarkerPoint5() {
        detector = createDetector(25000);
        MarkerPoint markerPoint1 = new MarkerPoint(MarkerType.INTRO, 10000, 1000, new ArrayList<>());
        MarkerPoint markerPoint2 = new MarkerPoint(MarkerType.CHAPTER, 30000, 20000, new ArrayList<>());
        detector.markerPointList = new ArrayList<>();
        detector.markerPointList.add(markerPoint1);
        detector.markerPointList.add(markerPoint2);

        MarkerPoint lastMarkerPoint = detector.getLastMarkerPoint();
        Assert.assertEquals( markerPoint2, lastMarkerPoint);
    }

    @NonNull
    private MarkerPointsDetector createDetector(long currentTime) {
        return new MarkerPointsDetector(new ITimeline() {
            @Override
            public void addListener(ITimelineListener listener) {

            }

            @Override
            public void addListener(ITimelineListener listener, Handler handler) {

            }

            @Override
            public void removeListener(ITimelineListener listener) {

            }

            @Override
            public ITimelinePosition getCurrentPosition() {
                return timeFactory.newPosition(currentTime);
            }

            @Override
            public ITimelinePosition getCurrentStartBound() {
                return null;
            }

            @Override
            public ITimelinePosition getCurrentEndBound() {
                return null;
            }

            @Override
            public ITimelinePosition getLivePosition() {
                return null;
            }

            @Override
            public boolean getVisibility() {
                return false;
            }

            @Override
            public void onDashMetadata(EnigmaMetadata metadata) {
            }

            @Override
            public void onHlsMetadata(EnigmaHlsMediaPlaylist metadata) {
            }
        });
    }
}
