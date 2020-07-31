### Custom UI controls series
# Spinner and Live Indicator
In this chapter we will show you how you can integrate with Enigma
Player two very simple components: a loading spinner for when the video
starts, and a live indicator for live streams.

## Spinner
Let's keep things simple, and for the spinner element use the
standard `android.widget.ProgressBar`.

Built-in, the progress bar has everything we need, with the standard Android configuration and style:

```
...
<ProgressBar
    ...
    style="?android:attr/progressBarStyle"
    ...
</ProgressBar>
...
```

In order to integrate our spinner with the Enigma Player, we need to
listen to player state events. You can read more about the player state
here: [Enigma Player State](../advanced_topics/enigma_player_state.md).


For example, in the playback activity:

```java
...
private ProgressBar pbLoader;
...
IEnigmaPlayer player = new EnigmaPlayer(session, exoPlayerTech);
...
enigmaPlayer.addListener(new BaseEnigmaPlayerListener(){
    ...
    @Override
    public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
        if (to == EnigmaPlayerState.LOADING || to == EnigmaPlayerState.BUFFERING){
            pbLoader.setVisibility(View.VISIBLE);
        }else if (to == EnigmaPlayerState.LOADED || from == EnigmaPlayerState.BUFFERING){
            pbLoader.setVisibility(View.GONE);
        }
    }
    ...
}
...
```

## Live indicator

The live indicator is a flag especially used when playing TV channels,
to indicate that the stream is live, or time-shifted back.

Again to keep things simple, we will use a plain
`android.widget.TextView` as our UI element of choice.

To integrate the indicator with our Enigma Player, we need to listen to
a certain event:

```java
...
playbackSessionListener = new BasePlaybackSessionListener(){
    @Override
    public void onPlayingFromLiveChanged(boolean live) {
        updateIsLive(live);
    }
};
...
player.addListener(new BaseEnigmaPlayerListener(){
    ...
    @Override
    public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
        if(from != null) {
            from.removeListener(playbackSessionListener);
        }
        if(to != null) {
            updateIsLive(to.isPlayingFromLive());
            to.addListener(playbackSessionListener, handler);
        }
    }
}, handler);
...
private void updateIsLive(boolean isLive){
    if (isLive){
        txtIsLive.setText("Live");
        txtIsLive.setTextColor(Color.RED);
    }else{
        txtIsLive.setText("Not live");
        txtIsLive.setTextColor(Color.DKGRAY);
    }
}
```

And that is all!


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Basics: play, pause and seeking](basics.md)<br/>
[Play/Pause Button](play_pause_button.md)<br/>
[Custom timeline](timeline.md)<br/>
&bull; Spinner and Live Indicator (current)<br/>
[Custom UI app](custom_ui_app.md)<br/>
