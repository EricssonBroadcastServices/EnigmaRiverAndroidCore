# Virtual controls
When implementing a custom UI the app developer need to know when to show/hide a button and when to display the button as 'enabled'.
These properties might depend on many things such as:
* the current player state
* the type of stream currently being played
* configuration made in the Red Bee OTT backend

Finding out these rules, implementing them and keeping them updated is a hassle. This is where virtual controls come in.

Virtual controls provide a 'headless UI' that automatically follows the rules listed above. To get hold of some virtual controls we use the method
IVirtualControls virtualControls = EnigmaPlayer.getVirtualControls();
```java
IVirtualControls virtualControls = EnigmaPlayer.getVirtualControls();
```

The `IVirtualControls` interface provides access to virtual buttons.
```java
public interface IVirtualControls {
    IVirtualButton getRewind();
    IVirtualButton getFastForward();
    IVirtualButton getPlay();
    IVirtualButton getPause();
    IVirtualButton getGoToLive();
    IVirtualButton getNextProgram();
    IVirtualButton getPreviousProgram();
    IVirtualButton getRestart();
    IVirtualButton getSeekBar();
    etc...
}
```

Each `IVirtualButton` represents an actual button in a UI.

```java
public interface IVirtualButton {
    boolean isEnabled();
    boolean isRelevant();
    void click();
    ...
}
```

By listening to the `onStateChanged` event an app developer can track the state of `isEnabled` and `isRelevant` and keep their corresponding UI button updated.

Virtual controls should be favored over directly controlling the EnigmaPlayer using `IEnigmaPlayer#getControls()`.



___
[Table of Contents](../index.md)<br/>
