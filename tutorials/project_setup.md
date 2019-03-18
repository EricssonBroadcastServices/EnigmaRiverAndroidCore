# Project setup

## Prerequisites
* Customer account and business unit created in customer portal
* End user account
* Android Studio 3.2 or higher
* Simple android app that manages to build and run on a device
* Internet connection

## Adding Enigma River libraries to your project
The Enigma River Android SDK uses [jitpack.io](https://jitpack.io/) for library distribution. So to add the libraries as dependencies to your project you also need to add jitpack as a repository.
```gradle
...
repositories {
	...
	maven { url "https://jitpack.io" }
	...
}
...
```

The Enigma River SDK does not handle video playback itself - it uses a 'player implementation' for this. So in addition to the `core` library you also need to add a player implementation library and any additional libraries needed for that particular player implementation. 

For using [ExoPlayer](https://github.com/google/ExoPlayer/tree/r2.9.1) as the player implementation, add the following lines to your `build.gradle` file.
```gradle
...
repositories {
	...
	google() //Needed for ExoPlayer
	jcenter() //Needed for ExoPlayer
	...
}
...
dependencies {
	...
	implementation "com.github.EricssonBroadcastServices:EnigmaRiverAndroidCore:1.0"
	implementation "com.github.EricssonBroadcastServices:EnigmaRiverAndroidExoPlayerIntegration:1.0"
    implementation 'com.google.android.exoplayer:exoplayer:2.9.1'
}
...
```

### Next
-> [Your first app](your_first_app.md)
