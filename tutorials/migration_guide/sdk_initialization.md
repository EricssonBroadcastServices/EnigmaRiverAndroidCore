### Migration guide (Android SDK 2.0 -> Enigma River Android SDK)
# Changes to SDK initialization
## Before
The old SDK was initialized in the `onCreate` method of the Application.
```
// ...
import net.ericsson.emovs.utilities.ContextRegistry;
import net.ericsson.emovs.exposure.auth.EMPAuthProviderWithStorage;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EMPRegistry.bindApplicationContext(this);
        EMPRegistry.bindExposureContext(Constants.API_URL, Constants.CUSTOMER, Constants.BUSSINESS_UNIT);
		// ...
	}

	// ...
}
```

## Now
In the new SDK the `EnigmaRiverContext` is similarly initialized in the `onCreate` method of the Application.
The lines
```
EMPRegistry.bindApplicationContext(this);
EMPRegistry.bindExposureContext(Constants.API_URL, Constants.CUSTOMER, Constants.BUSSINESS_UNIT);
```
are replaced by
```
EnigmaRiverContext.initialize(this, Constants.API_URL);
```

Notice that customer and business unit are no longer configured in the SDK context. But will have to
be provided by the app developer when needed.

## Changes to responsibilities

The old SDK kept the customer and business unit parameters as global variables. When using the new
SDK it is the responsibility of the app developer to keep track of these in whichever way they see
fit.


___
[Table of Contents](../index.md)<br/>
[Introduction](introduction.md)<br/>
[Structural changes](structural_changes.md)<br/>
Changes to SDK initialization (current)<br/>
[Changes to authentication/login](login.md)<br/>
[Changes to asset metadata retrieval](asset_metadata.md)<br/>
[Changes to playback](playback.md)<br/>
[Further reading](further_reading.md)<br/>
