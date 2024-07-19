// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;

import java.util.HashMap;
import java.util.Map;

public class DeviceParameters {

    private String deviceMake;
    private String deviceModel;

    public DeviceParameters(){
    }

    public DeviceParameters(String deviceMake, String deviceModel) {
        this.deviceMake = deviceMake;
        this.deviceModel = deviceModel;
    }

    public Map<String, ?> getParameters() {
        if (this.deviceMake == null) {
            this.deviceMake = EnigmaRiverContext.getDeviceInfo().getManufacturer();
        }
        if (this.deviceModel == null) {
            this.deviceModel = EnigmaRiverContext.getDeviceInfo().getDeviceModelPlay();
        }
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("deviceMake", this.deviceMake);
        parameters.put("deviceModel", this.deviceModel);
        return parameters;
    }

    public String getDeviceMake() {
        return this.deviceMake;
    }

    public void setDeviceMake(String deviceMake) {
        this.deviceMake = deviceMake;
    }

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }
}
