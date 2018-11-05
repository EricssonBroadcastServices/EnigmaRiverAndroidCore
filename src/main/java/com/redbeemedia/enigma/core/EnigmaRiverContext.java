package com.redbeemedia.enigma.core;

public class EnigmaRiverContext {
    public static IEnigmaPlayer createPlayer(IPlayerImplementation playerImplementation) {
        return new EnigmaPlayer(playerImplementation);
    }
}
