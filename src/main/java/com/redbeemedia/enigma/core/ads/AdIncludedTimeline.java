package com.redbeemedia.enigma.core.ads;

import android.os.Handler;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.player.timeline.BaseTimelineListener;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.timeline.ITimelineListener;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.timeline.TimelineListenerCollector;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.HandlerWrapper;

import java.util.ArrayList;
import java.util.List;

public class AdIncludedTimeline extends BaseTimelineListener implements IAdIncludedTimeline {

    private final TimelineListenerCollector collector = new TimelineListenerCollector();
    private final ITimeline internalTimeline;
    private final AdDetector adDetector;
    private boolean isActive = false;

    public AdIncludedTimeline(ITimeline timeline, AdDetector adDetector) {
        this.internalTimeline = timeline;
        this.internalTimeline.addListener(this);
        this.adDetector = adDetector;
    }

    @Override
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean isActive() { return isActive && adDetector.getAdBreaks() != null && adDetector.getAdBreaks().size() > 0; }

    @Override
    @Nullable public List<ITimelinePosition> getAdBreaks() {
        if(!isActive()) { return null; }

        List<ITimelinePosition> positions = new ArrayList<>();

        Duration previousAdBreaks = Duration.millis(0);
        for (AdBreak adBreak : adDetector.getAdBreaks()) {
            positions.add(adBreak.start.subtract(previousAdBreaks));
            previousAdBreaks = previousAdBreaks.add(adBreak.duration);
        }
        return positions;
    }

    @Override
    public void addListener(ITimelineListener listener) {
        collector.addListener(listener);
    }

    @Override
    public void addListener(ITimelineListener listener, Handler handler) {
        collector.addListener(listener, new HandlerWrapper(handler));
    }

    @Override
    public void removeListener(ITimelineListener listener) {
        collector.removeListener(listener);
    }

    @Override
    public Duration getPastAdDuration() {
        return getPastAdDuration(internalTimeline.getCurrentPosition());
    }

    @Override
    public ITimelinePosition getCurrentPosition() {
        if(!isActive()) {
            return internalTimeline.getCurrentPosition();
        }

        Duration pastAdsDuration = Duration.millis(0);
        ITimelinePosition actualPosition = internalTimeline.getCurrentPosition();
        if(actualPosition == null) { return null; }
        for(AdBreak adBreak : adDetector.getAdBreaks()) {
            if(actualPosition.beforeOrEqual(adBreak.start)) {
                return actualPosition.subtract(pastAdsDuration);
            }

            if(actualPosition.after(adBreak.start) &&
               actualPosition.before(adBreak.start.add(adBreak.duration))) {
                return adBreak.start.subtract(pastAdsDuration);
            }
            pastAdsDuration = pastAdsDuration.add(adBreak.duration);
        }
        return actualPosition.subtract(pastAdsDuration);
    }

    @Override
    public ITimelinePosition getCurrentStartBound() {
        return internalTimeline.getCurrentStartBound();
    }

    @Override
    @Nullable public AdBreak getCurrentAdBreak() {
        if(!isActive()) { return null; }

        ITimelinePosition actualPosition = internalTimeline.getCurrentPosition();
        if(actualPosition == null) { return null; }

        for(AdBreak adBreak : adDetector.getAdBreaks()) {
            if(actualPosition.afterOrEqual(adBreak.start) &&
              actualPosition.before(adBreak.start.add(adBreak.duration))) {
                return new AdBreak(adBreak.start.subtract(getPastAdDuration(adBreak.start)), adBreak.duration, adBreak.ads);
            }
        }

        return null;
    }

    @Override
    public ITimelinePosition getCurrentEndBound() {
        if(!isActive()) { return internalTimeline.getCurrentEndBound(); }
        return internalTimeline.getCurrentEndBound() != null ? internalTimeline.getCurrentEndBound().subtract(getTotalAdLength()) : null;
    }

    @Override
    public ITimelinePosition getLivePosition() {
        return internalTimeline.getLivePosition();
    }

    @Override
    public boolean getVisibility() {
        return internalTimeline.getVisibility();
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        collector.onVisibilityChanged(visible);
    }

    @Override
    public void onCurrentPositionChanged(ITimelinePosition timelinePosition) {
        if(!isActive()) {
            collector.onCurrentPositionChanged(timelinePosition);
        } else {
            collector.onCurrentPositionChanged(getCurrentPosition());
        }
    }

    @Override
    public void onBoundsChanged(ITimelinePosition start, ITimelinePosition end) {
        if(!isActive()) {
            collector.onBoundsChanged(start, end);
        } else {
            collector.onBoundsChanged(getCurrentStartBound(), getCurrentEndBound());
        }
    }

    @Override
    public void onLivePositionChanged(ITimelinePosition timelinePosition) {
        collector.onLivePositionChanged(timelinePosition);
    }

    public Duration getPastAdDuration(ITimelinePosition actualPosition) {
        if(!isActive()) { return Duration.millis(0); }
        Duration pastAdsDuration = Duration.millis(0);
        if(actualPosition == null) { return null; }
        for(AdBreak adBreak : adDetector.getAdBreaks()) {
            if(actualPosition.beforeOrEqual(adBreak.start)) {
                return pastAdsDuration;
            }
            pastAdsDuration = pastAdsDuration.add(adBreak.duration);
        }
        return pastAdsDuration;
    }

    public List<Long> detectContentBreaks() {
        List<Long> contentBreaks = new ArrayList<>();
        long prevStart = 0;
        for(AdBreak adBreak : adDetector.getAdBreaks()) {
            if(prevStart==0){
                prevStart = adBreak.duration.inWholeUnits(Duration.Unit.MILLISECONDS);
            }else{
                contentBreaks.add(adBreak.getStart().getStart() - prevStart);
                prevStart = adBreak.getStart().getStart() + adBreak.duration.inWholeUnits(Duration.Unit.MILLISECONDS);
            }
        }
        return contentBreaks;
    }

    public int getCountOfAdsPassed(ITimelinePosition timeOfContent) {
        long totalTimeContentPassed = 0;
        if (adDetector.getAdBreaks() == null || adDetector.getAdBreaks().isEmpty()) {
            return 0;
        }
        int totalAds = 0;
        if (adDetector.getAdBreaks().get(0).getStart().getStart() <= 0) {
            totalAds = 1;
        }
        for (Long content : adDetector.getContentBreaks()) {
            totalTimeContentPassed = totalTimeContentPassed + content;
            if (totalTimeContentPassed >= timeOfContent.getStart()) {
                break;
            }
            totalAds = totalAds + 1;
        }
        return totalAds;
    }

    public long getTotalAdDurationFromThisTime(ITimelinePosition timeOfContent) {
        int adNumber = getCountOfAdsPassed(timeOfContent);
        long totalAdDuration = 0;
        for(int i = 0 ; i <adNumber;i++){
            totalAdDuration = totalAdDuration + adDetector.getAdBreaks().get(i).duration.inWholeUnits(Duration.Unit.MILLISECONDS);
        }
        return totalAdDuration;
    }


    public ITimelinePosition getLastAdPositionFromThisPosition(ITimelinePosition actualPosition) {
        if (actualPosition == null) {
            return null;
        }
        AdBreak prevAdBreak = null;
        int counter = 0;
        for (AdBreak adBreak : adDetector.getAdBreaks()) {
            counter = counter + 1;
            if (actualPosition.afterOrEqual(adBreak.start.add(adBreak.duration))) {
                prevAdBreak = adBreak;
            } else {
                break;
            }
        }
        if (prevAdBreak != null) {
            return prevAdBreak.start;
        }
        return null;
    }

    public AdBreak getLastAdBreakFromThisPosition(ITimelinePosition actualPosition) {
        if (actualPosition == null) {
            return null;
        }
        AdBreak prevAdBreak = null;
        int counter = 0;
        for (AdBreak adBreak : adDetector.getAdBreaks()) {
            counter = counter + 1;

            ITimelinePosition adEndTime = adBreak.start.add(adBreak.duration);
            if (actualPosition.afterOrEqual(adEndTime) ||
                    (actualPosition.afterOrEqual(adBreak.start) && actualPosition.before(adEndTime))) {
                prevAdBreak = adBreak;
            } else {
                break;
            }
        }
        return prevAdBreak;
    }

    public AdBreak getLastAdBreakBetweenPositions(ITimelinePosition positionOne, ITimelinePosition positionTwo) {
        if (positionOne == null || positionTwo == null) {
            return null;
        }

        AdBreak currentFoundAdBreak = null;
        for (AdBreak adBreak : adDetector.getAdBreaks()) {
            if (adBreak.start.beforeOrEqual(positionOne)) {
                if (adBreak.start.afterOrEqual(positionTwo)) {
                    return adBreak;
                }
            } else if (adBreak.start.beforeOrEqual(positionTwo)) {
                currentFoundAdBreak = adBreak;
            }
        }
        return currentFoundAdBreak;
    }

    public AdBreak getAdBreakIfPositionIsBetweenTheAd(ITimelinePosition actualPosition) {
        if (actualPosition == null) {
            return null;
        }
        int counter = 0;
        if (adDetector.getAdBreaks() != null) {
            for (AdBreak adBreak : adDetector.getAdBreaks()) {
                counter = counter + 1;
                ITimelinePosition adEndTime = adBreak.start.add(adBreak.duration);
                if ((actualPosition.afterOrEqual(adBreak.start) && actualPosition.beforeOrEqual(adEndTime))) {
                    return adBreak;
                }
            }
        }
        return null;
    }

    private Duration getTotalAdLength() {
        Duration length = Duration.millis(0);
        for(AdBreak adBreak : adDetector.getAdBreaks()) {
            length = length.add(adBreak.duration);
        }
        return length;
    }
}
