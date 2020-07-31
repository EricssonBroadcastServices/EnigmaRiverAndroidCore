### Downloads and offline playback series
# Start playback of a downloaded asset
To play a downloaded asset, simply use a valid `DownloadedPlayable` in the `PlayRequest`
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
[Listing downloaded assets](list_downloads.md)<br/>
&bull; Start playback of a downloaded asset (current)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
