<!--
SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>

SPDX-License-Identifier: MIT
-->

# Playback Session
While playing a stream the EnigmaPlayer has a "PlaybackSession". Each EnigmaPlayer only has at most
one PlaybackSession at a time.

## PlaybackSession lifecycle
The start of a PlaybackSession is when an asset has been successfully loaded and is ready to be
played. The PlaybackSession of an EnigmaPlayer is considered to be alive/active until it is replaced,
either by a new PlaybackSession or by `null`.

![stateDiagram](../images/PlaybackSession_lifecycles.png
"PlaybackSession lifecycle")

To acquire a `IPlaybackSession` one needs to listen for the 'onPlaybackSessionChanged'-event from EnigmaPlayer.
```java
IEnigmaPlayer player = ...;
player.addListener(new BaseEnigmaPlayerListener() {
    @Override
    public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
        //Note that either one of 'from' and 'to' can be 'null'
    }
});
```
If EnigmaPlayer switches to a new PlaybackSession when there was no previous PlaybackSession,
`from` in the code above would be `null`. If EnigmaPlayer switches from a PlaybackSession to no PlaybackSession,
`to` in the code above would be `null`. The last value of 'to' will always be the value 'from' in
the next event.

## PlaybackSession playingFromLive

If the PlaybackSession is for a live stream it is possible to check if the player is currently
playing from the live edge. This is done by calling `IPlaybackSession#isPlayingFromLive()`. It is
also possible to add a listener to `IPlaybackSession` and listen for the 'onPlayingFromLiveChanged'-event.

#### Example - Text indicating if watching at live edge
```java
IEnigmaPlayer player = ...;

this.playbackSessionListener = new BasePlaybackSessionListener() {
    @Override
    public void onPlayingFromLiveChanged(boolean live) {
        updateText(live ? "Live" : "Not live");
    }
};


player.addListener(new BaseEnigmaPlayerListener() {
    @Override
    public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
        if(from != null) {
            from.removeListener(this.playbackSessionListener);
        }
        if(to != null) {
            updateText(to.isPlayingFromLive() ? "Live" : "Not live");
            to.addListener(this.playbackSessionListener, handler);
        }
    }
}, handler);
```


___
[Table of Contents](../index.md)<br/>
