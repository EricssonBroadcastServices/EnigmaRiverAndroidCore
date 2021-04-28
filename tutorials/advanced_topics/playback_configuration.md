# Playback Configuration
# Disable Analytics
If the analytics reporting is irrelevant for some reason, this can be disabled by setting a configuration flag in the `PlaybackProperties` object.

```java
IPlayable playable = new AssetPlayable("my_asset_id");

PlaybackProperties properties = new PlaybackProperties();

// Disable analytics
properties.setAnalyticsEnabled(false);

// Create a play request
IPlayRequest playRequest = new PlayRequest(playable, properties, new BasePlayResultHandler() {
    // ...
});

// Start playback
this.enigmaPlayer.play(playRequest);

```

# Adobe Primetime Configuration

In order to include the _X-Adobe-Primetime-MediaToken_ in the play request, one configures the playback properties, by setting an `AdobePrimetime` object (`PlaybackProperties.setAdobePrimetime(new AdobePrimetime("A_primetime_token"))`). If `PlaybackProperties.setAdobePrimetime(null)` is called, the Adobe Primetime token will be disabled.

If the token is invalid, the playback will fail.

```java
IPlayable playable = new AssetPlayable("my_asset_id");

PlaybackProperties properties = new PlaybackProperties();

String myBase64EncodedToken = "Base64encodedString";

// Configure the playback using the token.
properties.setAdobePrimetime(new AdobePrimetime(myBase64EncodedToken));

// Create a play request
IPlayRequest playRequest = new PlayRequest(playable, properties, new BasePlayResultHandler() {
    // ...
});

// Start playback
this.enigmaPlayer.play(playRequest);
```

## Chromecast
In order to provide the token to a Chromecast playback action, one configures the `EnigmaCastPlaybackProperties.Builder` by calling `EnigmaCastPlaybackProperties.Builder().setAdobePrimetime(new AdobePrimetime("A_primetime_token"))`.

```java
EnigmaCastPlaybackProperties.Builder properties = new EnigmaCastPlaybackProperties.Builder();

String myBase64EncodedToken = "Base64encodedString";

// Configure the playback using the token.
properties.setAdobePrimetime(new AdobePrimetime(myBase64EncodedToken));

IEnigmaCastRequest castRequest = EnigmaCastRequest.Builder("my_asset_id", my_session).setPlaybackProperties(playbackProperties.build()).build();
castManager.play(castRequest, new BaseEnigmaCastResultHandler() { 
    //...
});
```



___
[Table of Contents](../index.md)<br/>
