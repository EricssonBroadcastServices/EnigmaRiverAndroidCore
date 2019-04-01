package com.redbeemedia.enigma.core.player;

public interface IPlayerImplementation {
    void install(IEnigmaPlayerEnvironment environment);
    void release();
}
