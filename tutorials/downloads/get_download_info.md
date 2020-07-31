### Downloads and offline playback series
# Get available tracks for download
To get information about which different tracks are available to choose between when downloading an
asset, use `IEnigmaDownload#getDownloadableInfo`.
```java
IEnigmaDownload enigmaDownload = new EnigmaDownload(businessUnit);
enigmaDownload.getDownloadableInfo(assetID, session, new BaseResultHandler<IDownloadableInfo>() {
    @Override
    public void onResult(IDownloadableInfo result) {
        List<VideoDownloadable> videoTrackChoices = result.getVideoTracks();
        List<AudioDownloadable> audioTrackChoices = result.getAudioTracks();
        List<SubtitleDownloadable> subtitleTracksChoices = result.getSubtitleTracks();
        // ... use later when making a DownloadStartRequest ... //
    }

    @Override
    public void onError(EnigmaError error) {
        // ... handle error ... //
    }
}, handler);
```
From the `IDownloadableInfo` object we can get video tracks, audio tracks and subtitle tracks.

```java
public class VideoDownloadable implements IDownloadablePart {
    ...
    public String getName() {...}
    public int getBitrate() {...}
    public long getFileSize() {...}
    ...
}
```

```java
public class AudioDownloadable implements IDownloadablePart {
    ...
    public String getName() {...}
    public int getBitrate() {...}
    public String getLanguage() {...}
    public long getFileSize() {...}
    ...
}
```

```java
public class SubtitleDownloadable implements IDownloadablePart {
    ...
    public String getName() {...}
    ...
}
```




___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Gradle dependencies](dependencies.md)<br/>
[Check if an asset is downloadable](check_downloadability.md)<br/>
[Using the download API](enigma_download.md)<br/>
&bull; Get available tracks for download (current)<br/>
[Start asset download](start_download.md)<br/>
[Listing downloaded assets](list_downloads.md)<br/>
[Start playback of a downloaded asset](play_download.md)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
