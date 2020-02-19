# EnigmaPlayerState
![stateDiagram](../images/EnigmaPlayerState_diagram.png "EnigmaPlayer
state diagram")

EnigmaPlayer keeps an internal state (starting at `IDLE`). The current state of the EnigmaPlayer can be accessed using:
```java
public interface IEnigmaPlayer {
    ...
    EnigmaPlayerState getState();
    ...
}
```

It is possible to keep track of changes to the state using an EnigmaPlayerListener listener:
```java
IEnigmaPlayer player = ...;
player.addListener(new BaseEnigmaPlayerListener() {
    @Override
    public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
        Log.d(TAG, "State changed from "+from+" to "+to);
    }
}, handler);
```

### Example - Using EnigmaPlayerState for controlling UI

The following example code assumes we have a UI element called `progressSpinner` that we want to show while the player is loading an asset.

```java
IEnigmaPlayer player = ...;
player.addListener(new BaseEnigmaPlayerListener() {
    @Override
    public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
        if(to == EnigmaPlayerState.LOADING) {
            progressSpinner.show();
        } else if(from == EnigmaPlayerState.LOADING) {
            progressSpinner.hide();
        }
    }
}, handler);

```



___
[Table of Contents](../index.md)<br/>
