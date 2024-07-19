<!--
SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>

SPDX-License-Identifier: MIT
-->

# Ad Insertion
This tutorial describes how to facilitate and configure server-side ad insertion (SSAI) during playback. 

SSAI enabled streams allow the server to continuously provide the SDK with information about the playback of ads in a stream, allowing the SDK to report back the playback status of the ads.

Play controls should also be affected by the appearance of an ad. Scrubbing for example, should not be allowed during the playback of an ad.

# Configuration

## Set up additional SSAI playback parameter

In order to facilitate the SSAI logic, additional parameters are required during playback. These parameters are provided to the SDK through configuration of the `EnigmaRiverContext`.

By implementing the interface `IAdInsertionFactory`, the SDK consumer can provide an optional `IAdInsertionParameters`. The SDK calls `IAdInsertionParameters#createParameters(IPlayRequest)` each time a new playback is initiated.

The default `IAdInsertionParameters` implementation, `DefaultAdInsertionParameters`, is recommended to be used unless additional properties are required. 

If no ad insertion parameters are required, `IAdInsertionParameters#createParameters(IPlayRequest)` can return `null`.

Please note that each parameter in `IAdInsertionParameters` set to `null` will not be sent to the server.

`IAdInsertionParameters` accept any custom key(s) and value(s) and append those key/value to play request. 
`IAdInsertionParameters` interface accepts Map<String,String> where one can pass custom key(s)/value(s).

### Example configuration

```java
EnigmaRiverContext.EnigmaRiverContextInitialization initialization = new EnigmaRiverContext.EnigmaRiverContextInitialization(exposureBaseUrl);
initialization.setAdInsertionFactory(new IAdInsertionFactory() {

    @Override
    public IAdInsertionParameters createParameters(IPlayRequest request) {

        return new DefaultAdInsertionParameters(
        
            // latitude: The GPS based latitudinal position of the user
            "59.3239526",

            // longitutde: The GPS based longitudinal position of the user
            "18.1856332", 

            // mute: Indicate whether player is muted or not
            false,

            // consent: A consent string passed from various Consent Management Platforms (CMP’s)
            "", 

            // deviceMake: Manufacturer of device such as Apple or Samsung
            "Google",

            // ifa: User device ID
            "123abc",

            // gdprOptin: A flag for European Union traffic consenting to advertising
            true;

    }
});
EnigmaRiverContext.initialize(this, initialization);
```

## Disclaimer
Please note that this tutorial will be a subject for change and will be extended once SSAI is fully implemented.


___
[Table of Contents](../index.md)<br/>
