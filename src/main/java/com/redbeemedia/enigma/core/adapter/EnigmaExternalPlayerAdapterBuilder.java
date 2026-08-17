/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.adapter;

import com.redbeemedia.enigma.core.analytics.IPlayerAnalyticsListener;
import com.redbeemedia.enigma.core.playrequest.MaterialProfile;
import com.redbeemedia.enigma.core.session.ISession;

import java.util.Map;

public class EnigmaExternalPlayerAdapterBuilder {
    private final ISession session;
    private final IPlayerAnalyticsListener playerAnalyticsListener;
    private String adobePrimeToken = null;
    private Map<String, String> playRequestOptionalParameters = null;
    private  MaterialProfile materialProfile = null;
    public EnigmaExternalPlayerAdapterBuilder(ISession session, IPlayerAnalyticsListener playerAnalyticsListener) {
        this.session = session;
        this.playerAnalyticsListener = playerAnalyticsListener;
    }

    public void setAdobePrimeToken(String adobePrimeToken) {
        this.adobePrimeToken = adobePrimeToken;
    }

    public void setPlayRequestOptionalParameters(Map<String, String> playRequestOptionalParameters) {
        this.playRequestOptionalParameters = playRequestOptionalParameters;
    }

    public void setMaterialProfile(MaterialProfile materialProfile) {
        this.materialProfile = materialProfile;
    }

    public IEnigmaExternalPlayerAdapter build() {
        return new EnigmaExternalPlayerAdapter(
                session,
                playerAnalyticsListener,
                adobePrimeToken,
                playRequestOptionalParameters,
                materialProfile
        );
    }
}
