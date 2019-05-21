# Project setup

## Prerequisites
* Customer account and business unit created in customer portal
* End user account
* Android Studio 3.2 or higher
* Simple android app that manages to build and run on a device
* Internet connection

## Permissions required by the SDK
The following permissions are required for the SDK to function. Remember to add them to your `AndroidManifest.xml`.
* `<uses-permission android:name="android.permission.INTERNET" />` - Needed to access the Red Bee Media OTT backend.

## Adding Enigma River libraries to your project
The Enigma River Android SDK uses [jitpack.io](https://jitpack.io/) for library distribution. So to add the libraries as dependencies to your project you also need to add jitpack as a repository.
#### Note
There are 2 `build.gradle` files in your project. One in the 'parent-project' and one in the 'app-module'. You should be editing the `build.gradle` file located in your *app-module* (typically located at <code>/MyProjectName/<b>app</b>/build.gradle</code>)
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

For using [ExoPlayer](https://github.com/google/ExoPlayer/tree/r2.9.1) as the player implementation, add the following lines to your app-module `build.gradle` file.
```gradle
...
android {
    ...
    compileOptions {
        targetCompatibility JavaVersion.VERSION_1_8
    }
    ...
}
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
    implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:core:r1.0.15-BETA-13"
    implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exoplayerintegration:r1.0.15-BETA-13"
    implementation 'com.google.android.exoplayer:exoplayer:2.9.6'
}
...
```

### Next
-> [Your first app](your_first_app.md)
