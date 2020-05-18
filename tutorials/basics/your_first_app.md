### Basics series
# Your first app
In this tutorial we will create a very simple app using the SDK.

We will cover:
- Initializing the `EnigmaRiverContext`
- Acquiring a `Session` by logging in an end user.
- Starting playback of an asset using an asset id.

## Initialize `EnigmaRiverContext`

Before we can use the SDK we need to initialize the `EnigmaRiverContext`. This must be done exactly once and is typically done in the `Application` `onCreate`-method.
```java
...
import android.app.Application;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ...
        String exposureBaseUrl = ...; //Set this to the url supplied by your Red Bee OTT contact.
        EnigmaRiverContext.initialize(this, exposureBaseUrl);
        ...
    }
}
```

Also make sure that your app is actually using this subclass of
Application. This is done by setting the
<code>android:name</code>-attribute of the <code>&lt;application
/&gt;</code> tag in your `AndroidManifest.xml`. See
[Android tutorials](https://developer.android.com/guide/topics/manifest/application-element)
for further information.

## Retrieving a `Session`
Next, our end user needs a `Session` to be able to access content from
the Red Bee backend. If you are using Red Bee Medias default
authentication the `Session` object can be obtained using the
`EnigmaLogin` utility class.

`EnigmaLogin` will process a login request. Our intention is to launch a second activity after success completion of that request. Therefore we create a `android.os.Handler` and set it as the "callback handler" of `EnigmaLogin`. This ensures that any callback code originating from `EnigmaLogin` will be run on on the main thread.

```java
import android.app.Activity;
import android.os.Handler;
import com.redbeemedia.enigma.core.login.EnigmaLogin;

public class LoginActivity extends Activity {
    private static final String CUSTOMER_NAME = ...; //Your customer name
    private static final String BUSINESS_UNIT_NAME = ...; //The business unit name to use with this app
    
    ...
    private Handler handler;
    private EnigmaLogin enigmaLogin;
    ...
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...        
        this.enigmaLogin = new EnigmaLogin(CUSTOMER_NAME, BUSINESS_UNIT_NAME);
        
        this.handler = new Handler();
        this.enigmaLogin.setCallbackHandler(this.handler);
        ...
    }
}

```

If you don't have any end user accounts for your Red Bee OTT platform,
create one now. This can be done using the customer portal.

## Logging in
There are different types of login requests (`UserLoginRequest`,`AnonymousLoginRequest`, `ResumeLoginRequest`, etc.), but for this tutorial we will be using the `UserLoginRequest` wich represents a request to log in using end user credentials.

We add a method in our `LoginActivity` to send this request and handle the results.
```java
...
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.login.ILoginResultHandler;
import com.redbeemedia.enigma.core.login.UserLoginRequest;
import com.redbeemedia.enigma.core.session.ISession;
...
public class LoginActivity extends Activity {
...
    private void login(String username, String password) {
        enigmaLogin.login(new UserLoginRequest(username, password, new ILoginResultHandler() {
            @Override
            public void onSuccess(ISession session) {
                //TODO Handle successfull login!
            }

            @Override
            public void onError(EnigmaError error) {
                //TODO handle failed login.
            }
        }));
    }
...
}
```

To keep it simple in this tutorial, if the login request fails we will just show a `Toast` to the user.

```java
...
import android.widget.Toast;
import com.redbeemedia.enigma.core.error.InvalidCredentialsError;
...
            @Override
            public void onError(EnigmaError error) {
                if(error instanceof InvalidCredentialsError) {
                    Toast.makeText(LoginActivity.this, "Incorrect username/password", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
...
```

If the request was a success we want to start a new activity and supply the `Session` object. We create the stub for a new activity that will be used for playback: PlaybackActivity.

```java
import android.app.Activity;

public class PlaybackActivity extends Activity {
    public static final String EXTRA_SESSION = "session";
    
}
```

Now we can start the activity for the `onSuccess(ISession session)` callback method in our `LoginActivity`.
```java
...
import android.content.Intent;
...
            @Override
            public void onSuccess(ISession session) {
                Intent intent = new Intent(LoginActivity.this, PlaybackActivity.class);
                intent.putExtra(PlaybackActivity.EXTRA_SESSION, session);
                startActivity(intent);
            }
...
```

## Setting up an `EnigmaPlayer`

Since we are using ExoPlayer as our player implementation we will create a layout that uses the PlayerView component from the ExoPlayer library.

`activity_playback.xml`:
```xml
<com.google.android.exoplayer2.ui.PlayerView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/player_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:keepScreenOn="true"/>
```

In the `onCreate` method of our `PlaybackActivity` class we begin by connecting the layout, retrieving the `Session` and creating a Handler for the main thread.
```java
...
import android.os.Handler;
import android.content.Intent;
import com.redbeemedia.enigma.core.session.ISession;
...
public class PlaybackActivity extends Activity {
    public static final String EXTRA_SESSION = "session";

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        //Create a Handler for the main thread.
        this.handler = new Handler();

        //Retrieve the session from the intent.
        Intent intent = getIntent();
        ISession session = intent.getParcelableExtra(EXTRA_SESSION);
        
        ...to be continued...
    }
}
```
Next, we create an `ExoPlayerTech`. This class implements the `IPlayerImplementation` interface and acts as a bridge between `EnigmaPlayer` and the underlying ExoPlayer.

ExoPlayer requires an app name, so we will supply it with `"tutorialApp"` in this tutorial. We also need to attach a PlayerView where we want to display video. We use `findViewById(R.id.player_view)` to find the `PlayerView` component from the layout.

```java
...
import com.redbeemedia.enigma.exoplayerintegration.ExoPlayerTech;
...
public class PlaybackActivity extends Activity {
...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        
        //Create ExoPlayerTech and connect a compatible view.
        ExoPlayerTech exoPlayerTech = new ExoPlayerTech(this, "tutorialApp");
        exoPlayerTech.attachView(findViewById(R.id.player_view));
        
        ...to be continued...
    }
}
```

Now it is time to create an `EnigmaPlayer`. We wrap the creation of the `EnigmaPlayer` in a method called `createEnigmaPlayer`. To create an `EnigmaPlayer` we need a `Session` and a `PlayerImplementation`. We also want to set the callback handler so our method will also need to take a Handler parameter.

```java
...
import android.os.Handler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.player.IPlayerImplementation;
import com.redbeemedia.enigma.core.player.IEnigmaPlayer;
import com.redbeemedia.enigma.core.player.EnigmaPlayer;
...
public class PlaybackActivity extends Activity {
...
    private IEnigmaPlayer createEnigmaPlayer(ISession session, IPlayerImplementation playerImplementation, Handler callbackHandler) {
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(session, playerImplementation);
        enigmaPlayer.setActivity(this); //Binds the EnigmaPlayer to the lifecycle of this activity.
        enigmaPlayer.setCallbackHandler(callbackHandler);
        return enigmaPlayer;
    }
...
}
```

In `onCreate` we call this method and store it in a member variable of the activity.

```java
...
public class PlaybackActivity extends Activity {
...
    private IEnigmaPlayer enigmaPlayer;
    private Handler handler;
...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        
        //Create an EnigmaPlayer.
        this.enigmaPlayer = createEnigmaPlayer(session, exoPlayerTech, handler);
    }
...
}
```

`EnigmaPlayer` player is now configured and ready to use!

## Start playback using an asset id
In this tutorial we will start playback automatically whenever the `PlaybackActivity` is resumed.

```java
...
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.AssetPlayable;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.PlayRequest;
import com.redbeemedia.enigma.core.playrequest.BasePlayResultHandler;
import com.redbeemedia.enigma.core.error.EnigmaError;
...
public class PlaybackActivity extends Activity {
...
    @Override
    protected void onResume() {
        super.onResume();

        //Create a playable
        String assetId = ...; //For this tutorial, use a hardcoded asset id for one of your assets.
        IPlayable playable = new AssetPlayable(assetId);
        
        //Create a play request
        IPlayRequest playRequest = new PlayRequest(playable, new BasePlayResultHandler() {
            @Override
            public void onError(EnigmaError error) {
               //TODO Handle error
            }
        });
        
        //Start playback
        this.enigmaPlayer.play(playRequest);
    }
...
}

```

Now we only need to handle errors related to the `PlayRequest`. To keep it simple, we just show a `Toast` to the user.

```java
...
import android.widget.Toast;
import com.redbeemedia.enigma.core.error.AssetGeoBlockedError;
import com.redbeemedia.enigma.core.error.AssetNotAvailableError;
import com.redbeemedia.enigma.core.error.InvalidAssetError;
import com.redbeemedia.enigma.core.error.NoSupportedMediaFormatsError;
...
    @Override
    protected void onResume() {
        ...
        IPlayRequest playRequest = new PlayRequest(playable, new BasePlayResultHandler() {
            @Override
            public void onError(EnigmaError error) {
                if(error instanceof AssetGeoBlockedError) {
                    showMessage("This asset it not available for your region");
                } else if(error instanceof AssetNotAvailableError) {
                    showMessage("This asset is not available");
                } else if(error instanceof NoSupportedMediaFormatsError) {
                    showMessage("This asset cannot be played on your device");
                } else if(error instanceof InvalidAssetError) {
                    showMessage("Could not find asset "+((InvalidAssetError) error).getAssetId());
                } else {
                    showMessage("Could not start playback of asset");
                }
            }
        });
        ...
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
...
```

# And that's all there is to it!

This is what our finished app looks like:
[yourFirstApp](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidTutorialApps/tree/r3.0.4-BETA-9/yourFirstApp)<br />


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Introduction](introduction.md)<br/>
[Project setup](project_setup.md)<br/>
Your first app (current)<br/>
