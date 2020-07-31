### Custom UI controls series
# Play/Pause Button
It is not very elegant to have a player UI with two buttons, one for `play` and another for `pause`.<br />
This is why in this chapter you will learn how to replace the two mentioned buttons with a single one that will perform both actions.

It is very easy to implement, all you need is a simple custom button that extends `ImageButton`.
```java
...
public class PausePlayImageButton extends ImageButton
...
```

Then we need a way to connect it to the enigma player instance and listen to the needed player states: when playback is paused and when playback is resumed.

For that, our button should have a method like this:
```java
...
public void connectTo(IEnigmaPlayer enigmaPlayer){
        this.enigmaPlayer = enigmaPlayer;
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usingPauseButton){
                    enigmaPlayer.getControls().pause(new ControlResultHandler(TAG, "pause"));
                }else {
                    enigmaPlayer.getControls().start(new ControlResultHandler(TAG, "play"));
                }
            }
        });

        this.enigmaPlayer.addListener(new BaseEnigmaPlayerListener(){
            @Override
            public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
                if (to == EnigmaPlayerState.PLAYING){
                    usingPauseButton = true;
                    updatePlayPauseButtonImage();
                }else if(from == EnigmaPlayerState.PLAYING){
                    usingPauseButton = false;
                    updatePlayPauseButtonImage();
                }
            }
        }, handler);
    }
...
```
We still have a missing piece, the method to update the button image:
```java
...
private void updatePlayPauseButtonImage() {
        int icon = usingPauseButton ? R.drawable.exo_icon_pause : R.drawable.exo_icon_play;
        this.setImageResource(icon);
    }
...
```

Pretty much that's it with our simple custom button, but how do we use it? <br />
It is very simple, we just need to hook it to the player instance.
```java
...
IEnigmaPlayer player = new EnigmaPlayer(session, exoPlayerTech);
...
ibtnPlayPause.connectTo(player);
...
```
And that's all there is to it! <br /> Now you have improved a bit the
player controls UI. Follow the next chapters to learn how to make it
awesome!



___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Basics: play, pause and seeking](basics.md)<br/>
&bull; Play/Pause Button (current)<br/>
[Custom timeline](timeline.md)<br/>
[Spinner and Live Indicator](spinner_and_live.md)<br/>
[Custom UI app](custom_ui_app.md)<br/>
