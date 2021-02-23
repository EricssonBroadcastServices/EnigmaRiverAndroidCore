### Chromecast integration series
# Integrating Red Bee OTT cast support
Before integrating the Enigma River Android cast-module please make sure
you are familiar with the basics of implementing a chromecast sender
application in Android. We recommend completing the
['Cast-enable an Android app'](https://codelabs.developers.google.com/codelabs/cast-videos-android/)
tutorial in Google Developers Codelabs.

<br/>
Tip: Before trying to cast using your app, make sure you can cast with a widely used app such as YouTube.

#### Cast-module

The first thing needed in order to add cast functionality to your app is to add a dependency to the cast-module of the SDK.
```gradle
// in build.gradle of your app-module
...
dependencies {
    ...
    implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:cast:r3.1.12-BETA-USING-DRIFT-METER-LOG-OUTPUT-2"
    ...
}
...
```

The cast-module includes a transparent dependency to Google's
[Cast framework](https://developers.google.com/cast/docs/android_sender/integrate),
so you do not need to explicitly add that dependency.

#### Setting up cast

###### OptionsProvider

To integrate cast into our app we need to provide an `OptionsProvider` for the Google Cast framework. This is done by adding a `<meta-data>`-tag in your `AndroidManifest.xml`:
```
<application ...>
    ...
    <meta-data
        android:name="com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME"
        android:value="com.redbeemedia.enigma.cast.optionsprovider.EnigmaCastOptionsProvider" />
    ...
 </application>
```

The Enigma River Android SDK provides this `OptionsProvider` out-of-the-box. If you need to customize any part of the CastOptions, `EnigmaCastOptionsProvider` is suitable for extension.

###### MediaRouter

When a connection to a chromecast receiver has been established the `EnigmaCastManager` will be notified. How the connection is managed is up to you, but we recommend using the
standard button provided in the MediaRouter library.
```gradle
// in build.gradle of your app-module
...
dependencies {
    ...
    implementation 'androidx.mediarouter:mediarouter:1.0.0'
    ...
}
...
```

Make sure your activities inherits from `FragmentActivity` or any of its descendants (for example `AppCompatActivity`). This is required for the MediaRouterButton to function.
You may have to add a dependency to the `appcompat` library if you don't already have it:
```gradle
// in build.gradle of your app-module
...
dependencies {
    ...
    implementation 'androidx.appcompat:appcompat:1.0.0'
    ...
}
...
```

Then add the MediaRouterButton to all of your activities. See
[Google's tutorial section](https://developers.google.com/cast/docs/android_sender/integrate#add_a_cast_button)
on adding the cast button.

###### EnigmaCastManager

To initialize Google's Cast framework you need to call `CastContext.getSharedInstance(applicationContext)` or `EnigmaCastManager.getSharedInstance(applicationContext)` in `onCreate` of every activity or your app.
```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    ...
    EnigmaCastManager.getSharedInstance(getApplicationContext());
    ...
}
```

Your setup is now done.

#### Sending a PlayRequest to the chromecast receiver

To start playing an asset on the chromecast receiver, create an `EnigmaCastRequest`. You'll need to supply the assetID of the asset to be cast as well as a `ISession` object. This is done using a builder pattern:
```java
IEnigmaCastRequest castRequest = new EnigmaCastRequest.Builder(assetId, session).build();
```
You can also supply the builder with an `EnigmaCastPlaybackProperties` object using `.setPlaybackProperties(...)`.

To start casting simply call `play(...)` on the `IEnigmaCastManager`.
```java
IEnigmaCastManager enigmaCastManager = EnigmaCastManager.getSharedInstance(getApplicationContext());

IEnigmaCastRequest castRequest = new EnigmaCastRequest.Builder(assetId, session).build();

enigmaCastManager.play(castRequest, new BaseEnigmaCastResultHandler() {
    @Override
    public void onSuccess() {
        //Success
    }

    @Override
    public void onException(Exception e) {
        //Failed
    }
});
```

Congratulations, you should now be able to cast Red Bee OTT media assets!


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
&bull; Integrating Red Bee OTT cast support (current)<br/>
[Advanced usage of the cast-module](chromecast_advanced.md)<br/>
