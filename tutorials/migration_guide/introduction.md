### Migration guide (Android SDK 2.0 -> Enigma River Android SDK)
# Introduction
The previous Android SDK (called *the old SDK* in this guide) provided for integrating with the
Red Bee OTT backend has been deprecated. Developers of new apps should instead use
the Enigma River Android SDK (called *the new SDK* in this guide). Since there are still some app
developers using the old SDK we provide this migration guide to ease the transition to the new SDK.

## Target audience

Please note that this tutorial series is *only* relevant to app developers who have an existing app that uses the
old Android SDK (libraries named `AndroidClient{$moduleName}`).

## Names (EMP -> Enigma)

Names that were previously prefixed by "EMP" or "Emp" are now commonly prefixed with "Enigma" from the
name of the Red Bee OTT platform.

## Changes to code design philosophy
### Requests and result handlers
The Enigma River Android SDK tries to stay consistent by using 'request objects' for representing a
request being made to a component, together with 'result handlers' that handles the result of the request.
In the old SDK, this would typically be a method call with a listener/callback-object.
#### Why this change?
Using a request object to represent a request makes it possible to provide many request-options
without needing a big set of overloaded methods. This keeps the API small and understandable.

### The SDK no longer provides UI components
The old SDK provided UI components for playback (such as `SimplePlaybackActivity`). With the
new SDK using and maintaining UI components are the responsibility of the app developer.
#### Why this change?
By avoiding UI components the SDK maintains the highest compatibility with different android versions
and frameworks. By keeping the SDK "UI-agnostic" it also stays flexible to accommodate for different
UI architectures.

### Singletons are kept at a minimum
The old SDK was designed with a lot of singletons (such as `EMPRegistry`, `EMPAuthProvider` and `EMPEntitlementProvider`)
accessed through `.getInstance()`. The new SDK instead only contain one singleton: The `EnigmaRiverContext`.
#### Why this change?
By providing at least one singleton give the project a common entry point for using global objects.
On the other hand, too many singletons make the code monolithic and hard to keep loosely coupled.

### Base-classes are used for anonymous classes
In the old SDK callbacks, listeners and result handlers were implemented by anonymous classes by
extending interfaces directly. In the new SDK base-classes are provided (prefixed with `Base`) and
recommended to be used instead of directly extending interfaces.
#### Why this change?
By extending Base-classes instead of interfaces code can maintain backwards compatibility when new
methods are added to an interface.

### Keeping track of objects are now the responsability of the app developer
The old SDK kept global references to things such as business unit and session. In the new SDK it
is up to the app developer to keep such references.
#### Why this change?
To keep the SDK as flexible as possible while imposing little in terms of app structure, it is up
to the app developer to decide which objects should be global and how they should be managed.

### Complete separation between playback technology and EnigmaPlayer
While the old SDK provided the concept of `Tech`s, the logic relating to the Red Bee OTT backend
was tightly bound to each single tech. The new SDK introduces the concept of a `IPlayerImplementation`
as an abstraction for the exact video playback logic. All things related to assets and play-calls
are contained in the player-agnostic `core`-module. App developers should also integrate with the core
components to keep their apps player-agnostic.
#### Why this change?
The old SDK had problems with monoliticity. Changing to a different player tech other than ExoPlayer
required a lot of duplication of code containing specific logic related to the Red Bee OTT backend.
The new SDK provides a clear integration point for other player-techs/player-implementations that
will make it possible to integrate different players.

## Known limitations of the new SDK
* Two-factor authentication is not yet supported/provided out-of-the-box by the new SDK. App developers
can still retrieve a sessionToken any way they want through, but they will have to make the backend
REST-calls manually.


___
[Table of Contents](../index.md)<br/>
Introduction (current)<br/>
[Structural changes](structural_changes.md)<br/>
[Changes to SDK initialization](sdk_initialization.md)<br/>
[Changes to authentication/login](login.md)<br/>
[Changes to asset metadata retrieval](asset_metadata.md)<br/>
[Changes to playback](playback.md)<br/>
[Further reading](further_reading.md)<br/>
