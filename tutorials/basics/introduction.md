### Basics series
# Introduction
The Enigma River Android SDK provides a simple framework for app developers that facilitates communication with the Red Bee Media OTT backend. It takes away the burden of writing a lot of boilerplate code from the app developer. For example: making http calls, parsing the response and creating native objects. It simplifies the integration of a video player that plays streams from the backend, so that the app developer can focus on making awesome apps!

## Concepts

### End user
A user of your app that has created a user account for your RedBee OTT platform.

### Session
A Session represents a time period during which an end user is 'logged in' to the backend for a particular business unit. The Session contains a "session token" that is used for authorization while communicating with the backend.

### Enigma River Context
#### Note: Not to be confused with android.content.Context!

This is the central context that the SDK uses. It contains, among other things, which url to use when communicating with the backend. The Enigma River Context needs to be initialized at the start of the application before using other parts of the SDK.

### EnigmaPlayer
EnigmaPlayers represents video players integrated with the Enigma River SDK. EnigmaPlayers themselves do not actually provide functionality to play video and need a "player implementation" injected to do this. Instead, an EnigmaPlayer provides a player implementation independent interface for controlling playback, listening to different playback events and integrating with other components of the SDK (such as Red Bee Analytics).

### Player implementation
We say that EnigmaPlayers are 'player agnostic' meaning that it can work with different player technologies, for example, ExoPlayer. The bridge between such a player technology and an EnigmaPlayer is called a "player implementation".

### Request handlers, requests and result handlers
The SDK contains a set of "request handlers" that facilitates making different types of logically atomic requests. For example, EnigmaLogin is a request handler that facilitates making request to log in. A request handler can handle a certain type of request. A request object represents a particular request and contains a "result handler". The results of a request is handled by a result handler. This object typically has callback-methods, to handle asynchronous errors that can occuring while trying to complete the request, and to recieve the resulting object acquired by the request upon successful completion.

### Callback handlers
Handler (android.os.Handler) can be specified as the callback handler of a request handler. This ensures that when the request handler calls a method on the result handler for a request, it does so on the thread connected to the Handler.

### Playable
A Playable represents a type on object that an EnigmaPlayer can play. This is typically an "AssetPlayable" containing an asset id.


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
&bull; Introduction (current)<br/>
[Project setup](project_setup.md)<br/>
[Your first app](your_first_app.md)<br/>
