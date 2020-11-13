package com.redbeemedia.enigma.core.playrequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default model of <code>IAdInsertionParameters</code>.
 */
public class DefaultAdInsertionParameters implements IAdInsertionParameters {

    private String latitude;
    private String longitude;
    private boolean mute;
    private String consent;
    private String deviceMake;
    private String ifa;
    private boolean gdprOptin;

    public DefaultAdInsertionParameters(String latitude,
                                        String longitude,
                                        boolean mute,
                                        String consent,
                                        String deviceMake,
                                        String ifa,
                                        boolean gdprOptin) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.mute = mute;
        this.consent = consent;
        this.deviceMake = deviceMake;
        this.ifa = ifa;
        this.gdprOptin = gdprOptin;
    }

    public Map<String,?> getParameters() {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("latitude", latitude);
        parameters.put("longitude", longitude);
        parameters.put("consent", consent);
        parameters.put("deviceMake", deviceMake);
        parameters.put("ifa", ifa);
        parameters.put("mute", String.valueOf(mute));
        parameters.put("gdprOptin", String.valueOf(gdprOptin));

        return parameters;
    }
}
