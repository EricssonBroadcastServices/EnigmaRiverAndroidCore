package com.redbeemedia.enigma.core.marker;

import android.util.Log;

import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Detect marker points / cue-points inserted in a content/asset document.
 */
public class MarkerPointsDetector implements IMarkerPointsDetector {

    List<MarkerPoint> markerPointList;
    private final ITimeline timeline;

    public MarkerPointsDetector(ITimeline timeline){
        this.timeline = timeline;
    }

    public List<MarkerPoint> getMarkerPointsList() {
        if (markerPointList == null) {
            return new ArrayList<>();
        }
        return markerPointList;
    }

    @Override
    public MarkerPoint getCurrentMarkerPoint() {
        ITimelinePosition currentPosition = timeline.getCurrentPosition();
        for (MarkerPoint markerPoint : getMarkerPointsList()) {
            if (markerPoint.getOffset() < currentPosition.getStart() && currentPosition.getStart() < markerPoint.getEndOffset()) {
                return markerPoint;
            }
        }
        return null;
    }

    @Override
    public MarkerPoint getLastMarkerPoint() {
        ITimelinePosition currentPosition = timeline.getCurrentPosition();
        MarkerPoint prevMarkerPoint = null;
        for (MarkerPoint markerPoint : getMarkerPointsList()) {
            if (currentPosition.getStart() >= markerPoint.getOffset() ) {
                prevMarkerPoint = markerPoint;
            } else if (markerPoint.getOffset() > currentPosition.getStart()) {
                break;
            }
        }
        return prevMarkerPoint;
    }

    @Override
    public void parseJSONObject(JSONObject jsonObject) {
        try {
            JSONArray markerPoints = jsonObject.getJSONArray("markerPoints");
            List<MarkerPoint> markerPointList = new ArrayList<>();
            for (int i = 0; i < markerPoints.length(); ++i) {
                JSONObject markerFormat = markerPoints.getJSONObject(i);
                String type = markerFormat.optString("type");
                int offset = markerFormat.optInt("offset");
                int endOffset = markerFormat.optInt("endOffset");
                JSONArray localizedArray = markerFormat.getJSONArray("localized");
                List<Localized> localizedList = new ArrayList<>();
                for (int j = 0; j < localizedArray.length(); ++j) {
                    JSONObject localizedArrayJSONObject = localizedArray.getJSONObject(j);
                    String locale = localizedArrayJSONObject.optString("locale");
                    String title = localizedArrayJSONObject.optString("title");
                    Localized localized = new Localized(locale, title);
                    localizedList.add(localized);
                }
                MarkerPoint markerPointObj = new MarkerPoint(MarkerType.valueOf(type), endOffset, offset, localizedList);
                markerPointList.add(markerPointObj);
            }
            this.markerPointList = markerPointList;
        } catch (Exception e) {
            Log.w("MarkerPoints", "Could not prase JSON : " + e.getMessage());
        }
    }
}
