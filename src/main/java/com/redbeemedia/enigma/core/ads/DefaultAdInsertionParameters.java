// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Default model of <code>IAdInsertionParameters</code>.
 */
public class DefaultAdInsertionParameters implements IAdInsertionParameters {

    private String latitude;
    private String longitude;
    private boolean mute;
    private String consent;
    private String deviceType;
    private String ifa;
    private String ifaType;

    public DefaultAdInsertionParameters(String latitude,
                                        String longitude,
                                        boolean mute,
                                        String consent,
                                        String ifa) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.mute = mute;
        this.consent = consent;
        this.ifa = ifa;
    }

    public DefaultAdInsertionParameters(String latitude,
                                        String longitude,
                                        boolean mute,
                                        String consent,
                                        String ifa,
                                        String deviceType) {
        this(latitude, longitude, mute, consent, ifa);
        this.deviceType = deviceType;
    }

    @Deprecated
    public DefaultAdInsertionParameters(String latitude,
                                        String longitude,
                                        boolean mute,
                                        String consent,
                                        String deviceMake,
                                        String ifa,
                                        String ifaType,
                                        boolean gdprOptin) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.mute = mute;
        this.consent = consent;
        this.ifa = ifa;
        this.ifaType = ifaType;
    }

    public Map<String, ?> getParameters() {
        if (this.deviceType == null) {
            this.deviceType = EnigmaRiverContext.getDeviceInfo().getDeviceTypePlay();
        }
        if (this.ifaType == null) {
            // https://tools.cloud.ebms.ericsson.net/confluence/display/INTDOC/SSAI+Ad+Parameters
            this.ifaType = "aaid";
        }
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("latitude", this.latitude);
        parameters.put("longitude", this.longitude);
        parameters.put("consent", this.consent);
        parameters.put("deviceType", this.deviceType);
        parameters.put("ifa", this.ifa);
        parameters.put("ifaType", this.ifaType);
        parameters.put("mute", String.valueOf(this.mute));
        return parameters;
    }

    public String getIfaType() {
        return this.ifaType;
    }

    public void setIfaType(String ifaType) {
        this.ifaType = ifaType;
    }

    public String getIfa() {
        return this.ifa;
    }

    public void setIfa(String ifa) {

        this.ifa = ifa;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }


    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public String getConsent() {
        return consent;
    }

    public void setConsent(String consent) {
        this.consent = consent;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
