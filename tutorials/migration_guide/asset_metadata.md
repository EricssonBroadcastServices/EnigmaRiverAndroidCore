### Migration guide (Android SDK 2.0 -> Enigma River Android SDK)
# Changes to asset metadata retrieval
## Before

The old SDK provided methods for fetching asset metadata from exposure by calling
```
EMPMetadataProvider.getInstance().getAssets("/content/asset?fieldSet=ALL&&includeUserData=true&pageNumber=1&sort=originalTitle&pageSize=100&onlyPublished=true&assetType=CLIP", new IMetadataCallback<ArrayList<EmpAsset>>() {
	@Override
    public void onMetadata(ArrayList<EmpAsset> metadata) {
        // Do things with the assets, for instance show them in an view via an adapter
    }

    @Override
    public void onError(ExposureError error) {
        // Handle error
    }
);
```

## Now
The core module of the new SDK does not (yet) contain a model for assets. There is however an
optional module called `EnigmaRiverAndroidExposureUtils` that handles the conversion of the exposure
backend Json objects to native java object. To use this module, app developers need to add it as a
dependency in their gradle build file:
```
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exposureUtils:r3.6.4-BETA-4"
```
Performing the same call as in the example from the old SDK above is done by
```
    EnigmaExposure exposure = new EnigmaExposure(session); // Or 'new EnigmaExposure(businessUnit)'
                                                           // if no authentication is required for the endpoint.
    IExposureResultHandler<ApiAssetList> resultHandler = new BaseExposureResultHandler<ApiAssetList>() {
        @Override
        public void onSuccess(ApiAssetList result) {
            List<ApiAsset> assets = result.getItems(); //ApiAsset corresponds to the earlier EmpAsset
            // Handle result
        }

        @Override
        public void onError(EnigmaError error) {
            // Handle error
        }
    };
    GetAllAssetsRequest request = new GetAllAssetsRequest(resultHandler)
            .setFieldSet(FieldSet.ALL)
            .setIncludeUserData(true)
            .setPageNumber(1)
            .setSort("originalTitle")
            .setPageSize(100)
            .setOnlyPublished(true)
            .setAssetType(AssetType.CLIP);

    exposure.doRequest(request);
```


___
[Table of Contents](../index.md)<br/>
[Introduction](introduction.md)<br/>
[Structural changes](structural_changes.md)<br/>
[Changes to SDK initialization](sdk_initialization.md)<br/>
[Changes to authentication/login](login.md)<br/>
&bull; Changes to asset metadata retrieval (current)<br/>
[Changes to playback](playback.md)<br/>
[Further reading](further_reading.md)<br/>
