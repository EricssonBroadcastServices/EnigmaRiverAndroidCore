### Downloads and offline playback series
# Check if an asset is downloadable
To check if an asset is available for download, two things are needed:
1. The asset JSON needs to be acquired from the Red Bee Media OTT backend.

For example, through `/v1/customer/{customer}/businessunit/{businessUnit}/content/asset?fieldSet=ALL`

2. The list of *availabilityKeys* for the user needs to be acquired from the Red Bee Media OTT backend.

This is done through the endpoint `/v2/customer/{customerUnit}/businessunit/{businessUnit}/entitlement/availabilitykey`

## ExposureUtils

There is an optional module available that provides conversion of JSON objects to POJOs. To include
this module in your project, add the following to your `build.gradle`:
```gradle
// in build.gradle of your app-module
...
dependencies {
    ...
    implementation 'com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exposureUtils:r3.4.0-BETA-8'
    ...
}
...
```

Once you have `exposureUtils` you can use `EnigmaDownloadHelper.isAvailableToDownload` to check if
an asset is available to download.
```java
long nowUtcMillis = System.currentTimeMillis();
boolean availableToDownload = EnigmaDownloadHelper.isAvailableToDownload(apiAsset, nowUtcMillis, userAvailabilityKeys)
```

ExposureUtils also provides classes and methods to interact with exposure backend.
##### Example - Get availability keys
The following example shows how to get the availability keys for a user from the backend.

**Note that we use the EnigmaExposure constructor that takes a ISession** since we need to be logged
in to use this endpoint.
```java
EnigmaExposure exposure = new EnigmaExposure(session); //Get entitlement/availabilitykey requires Authorization
exposure.doRequest(new GetAvailabilityKeys(new BaseExposureResultHandler<ApiAvailabilityKeys>() {
    @Override
    public void onSuccess(ApiAvailabilityKeys result) {
        List<String> availabilityKeys = result.getAvailabilityKeys();
        // ... use availabilityKeys ... //
    }

    @Override
    public void onError(EnigmaError error) {
        // ... handle error ... ///
    }
}));
```
##### Example - Get assets
The following example shows how to get the first page of assets from the backend.
```java
EnigmaExposure exposure = new EnigmaExposure(businessUnit);
exposure.doRequest(new GetAllAssetsRequest(new BaseExposureResultHandler<ApiAssetList>() {
    @Override
    public void onSuccess(ApiAssetList result) {
        List<ApiAsset> assets = result.getItems();
        // ... use assets ... //
    }

    @Override
    public void onError(EnigmaError error) {
        // ... handle error ... //
    }
})
.setOnlyPublished(true)
.setFieldSet(FieldSet.ALL) //Important! Needed to check isAvailableToDownload
.setPageSize(100)
.setPageNumber(1));
```

## Lightweight option

If you don't want to include `exposureUtils` in your project we recommend you copy the file
[EnigmaDownloadHelper.java](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidExposureUtils/blob/r3.4.0-BETA-8/src/main/java/com/redbeemedia/enigma/exposureutils/download/EnigmaDownloadHelper.java)
into your project and edit it appropriately.


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Gradle dependencies](dependencies.md)<br/>
&bull; Check if an asset is downloadable (current)<br/>
[Using the download API](enigma_download.md)<br/>
[Get available tracks for download](get_download_info.md)<br/>
[Start asset download](start_download.md)<br/>
[Managing ongoing downloads](ongoing_downloads.md)<br/>
[Listing downloaded assets](list_downloads.md)<br/>
[Start playback of a downloaded asset](play_download.md)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
