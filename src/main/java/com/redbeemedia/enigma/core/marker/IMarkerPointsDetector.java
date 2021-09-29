package com.redbeemedia.enigma.core.marker;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Detect marker points from endpoint
 */
public interface IMarkerPointsDetector {
    /**
     * Method return all marker points
     * @return
     */
    List<MarkerPoint> getMarkerPointsList();

    /**
     * this method return current MarkerPoint otherwise it return null, if there is no marker point
     * @return
     */
    MarkerPoint getCurrentMarkerPoint();


    /**
     * this method return last MarkerPoint otherwise it return null, if there is no marker point before this current time
     * @return
     */
    MarkerPoint getLastMarkerPoint();

    /**
     *  parse JSONObject
     * @param jsonObject
     * @throws JSONException
     */
    void parseJSONObject(JSONObject jsonObject);
}
