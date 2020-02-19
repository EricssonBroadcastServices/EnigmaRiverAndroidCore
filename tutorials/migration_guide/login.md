### Migration guide (Android SDK 2.0 -> Enigma River Android SDK)
# Changes to authentication/login
## Before
```
authProvider.checkAuth(new IAuthenticationListener() {
	@Override
	public void onAuthSuccess(String sessionToken) {
		// Although the sessionToken is passed, it is not really necessary to keep it in most scenarios as the library takes care of the token lifecycle
	}

	@Override
	public void onAuthError(ExposureError error) {
		// Perform login if error means user is not authenticated
	}
});
```

## Now
`EMPAuthProvider` is removed and instead an instance of `EnigmaLogin` is used to login using the
Red Bee OTT backend. `EnigmaLogin` is a request handler that handles implementations of `ILoginRequest`.
The usual login request is `UserLoginRequest` which is takes a username, a password and a
`ILoginResultHandler` in its constructor.
```
EnigmaLogin enigmaLogin = new EnigmaLogin(Constants.CUSTOMER, Constants.BUSSINESS_UNIT);
enigmaLogin.login(new UserLoginRequest(usernameString, passwordString, new ILoginResultHandler() {
    @override
    public void onSuccess(ISession session) {
        // The ISession object contains the customer and business unit associated with it as well as the
        // session token string.
    }

    @Override
    void onError(EnigmaError error) {
        // Handle error
    }
}));
```

The old SDK provided 'persistent login'. This can now be achieved by storing the session token string
between app restarts and using `ResumeLoginRequest`.
```
EnigmaLogin enigmaLogin = new EnigmaLogin(Constants.CUSTOMER, Constants.BUSSINESS_UNIT);
enigmaLogin.login(new ResumeLoginRequest(storedSessionToken, new ILoginResultHandler() {
     @override
     public void onSuccess(ISession session) {
         // Session token was still valid and now wrapped in a session object
     }

     @Override
     void onError(EnigmaError error) {
        if(error instanceof CredentialsError) {
            // Token not valid any more. Prompt user to log in again.
        } else {
            // Something else went wrong
        }
     }
 }));
```

## Changes to responsibilities
It is now the app developers responsibility to keep track of the Session object as well
as persisting the session token between app restarts if such functionality is wanted.



___
[Table of Contents](../index.md)<br/>
[Introduction](introduction.md)<br/>
[Structural changes](structural_changes.md)<br/>
[Changes to SDK initialization](sdk_initialization.md)<br/>
Changes to authentication/login (current)<br/>
[Changes to asset metadata retrieval](asset_metadata.md)<br/>
[Changes to playback](playback.md)<br/>
[Further reading](further_reading.md)<br/>
