package com.redbeemedia.enigma.core.context;

public class MockEnigmaRiverContext {
    public static synchronized void resetInitialize(MockEnigmaRiverContextInitialization initialization) {
        EnigmaRiverContext.resetInitialization(initialization);
    }
}
