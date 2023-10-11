### Additional topics for download and offline playback series
# Only download on wifi networks
## Limitations
Setting requirements for downloads is currently only possible if you are using the
`exoPlayerDownload` module as your download implementation.

## Setting ExoPlayer download requirements
ExoPlayer provides a way to set certain requirements that need to be fulfilled for a download
to progress. These can be set by calling `setRequirements` on the `DownloadManager` being used.
The `exoPlayerDownload` module keeps a reference to it's `DownloadManager` in it's context:
`ExoPlayerDownloadContext`.

After the Enigma River Android SDK has been initialized this `DownloadManager` can be retrieved,
and requirements can be set.
```java
public class MyApplication extends Application {
...
    @Override
    public void onCreate() {
        ...
        // Initialize Enigma River Android SDK
        EnigmaRiverContext.initialize(this, initialization);

        //Set download requirements
        DownloadManager downloadManager = ExoPlayerDownloadContext.getDownloadManager();
    }
...
}
```

### Only download while on wifi networks
By adding the requirement `Requirements.NETWORK_UNMETERED` we limit downloads to only progress when
on unmetered networks such as wifi.
```java
downloadManager.setRequirements(new Requirements(Requirements.NETWORK_UNMETERED));
```

For more available requirement options, see ExoPlayers documentation.


___
[Table of Contents](../index.md)<br/>
[Working with DRM protected offline content](download_drm_management.md)<br/>
[AssetDownloadState](asset_download_state.md)<br/>
[Offline Analytics events](offline_analytics.md)<br/>
&bull; Only download on wifi networks (current)<br/>
[AssetQueueDownload](asset_queue_download.md)<br/>
