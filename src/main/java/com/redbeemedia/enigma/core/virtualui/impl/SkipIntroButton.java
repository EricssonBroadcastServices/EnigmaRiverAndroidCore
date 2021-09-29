package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.marker.IMarkerPointsDetector;
import com.redbeemedia.enigma.core.marker.MarkerPoint;

/*package-protected*/ class SkipIntroButton extends AbstractVirtualButtonImpl {

    public SkipIntroButton(IVirtualButtonContainer container) {
        super(container, false);
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        return true;
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        IMarkerPointsDetector markerPointsDetector = container.getEnigmaPlayer().getMarkerPointsDetector();
        MarkerPoint currentMarkerPoint = markerPointsDetector.getCurrentMarkerPoint();
        if (currentMarkerPoint != null) {
            return currentMarkerPoint.isIntro();
        }
        return false;
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
        IMarkerPointsDetector markerPointsDetector = container.getEnigmaPlayer().getMarkerPointsDetector();
        MarkerPoint currentMarkerPoint = markerPointsDetector.getCurrentMarkerPoint();
        if (currentMarkerPoint != null) {
            if (currentMarkerPoint.isIntro()) {
                container.getPlayerControls().seekTo(currentMarkerPoint.getEndOffset());
            }
        }
    }
}
