### Downloads and offline playback series
# Start playback of a downloaded asset
To play a downloaded asset, simply use a valid `DownloadedPlayable` in the `OfflinePlayRequest`
to `IEnigmaPlayer#play` and start playback as described in [Your first app](../basics/your_first_app.md).

```java
IPlayRequest playRequest = new PlayRequest(session, downloadPlayable, new BasePlayResultHandler() {
    @Override
    public void onError(EnigmaError error) {
        // ... handle error ... //
    }
});
enigmaPlayer.play(playRequest);
```

#PlaybackProperties 

You can customize playback properties using OfflinePlaybackProperties. 
For downloaded assets, you can choose to start playback from the beginning (PlayFromPreference.BEGINNING) or from a specific offset (PlayFromPreference.OFFSET). 
In your application, it's advisable to store the playhead position locally and resume playback from there using IPlaybackProperties.PlayFrom.OFFSET(duration).

```java
IPlaybackProperties.PlayFromOffset offset = IPlaybackProperties.PlayFrom.OFFSET(Duration.seconds(50));
OfflinePlaybackProperties playbackProperties = new OfflinePlaybackProperties();
playbackProperties.setPlayFrom(offset);
IPlayRequest playRequest = new PlayRequest(session, downloadPlayable, playbackProperties, new BasePlayResultHandler() {
    @Override
    public void onError(EnigmaError error) {
        // ... handle error ... //
    }
});
enigmaPlayer.play(playRequest);
```

Available `DownloadedPlayable`s can be acquired by calling `IEnigmaDownload#getDownloadedAssets`.

See [Listing downloaded assets](list_downloads.md) for more details.


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Gradle dependencies](dependencies.md)<br/>
[Check if an asset is downloadable](check_downloadability.md)<br/>
[Using the download API](enigma_download.md)<br/>
[Get available tracks for download](get_download_info.md)<br/>
[Start asset download](start_download.md)<br/>
[Managing ongoing downloads](ongoing_downloads.md)<br/>
[Listing downloaded assets](list_downloads.md)<br/>
&bull; Start playback of a downloaded asset (current)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
