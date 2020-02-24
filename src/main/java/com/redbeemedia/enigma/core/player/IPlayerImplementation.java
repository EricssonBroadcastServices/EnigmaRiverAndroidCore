package com.redbeemedia.enigma.core.player;

import androidx.annotation.NonNull;

public interface IPlayerImplementation {
    void install(@NonNull IEnigmaPlayerEnvironment environment);
    void release();
}
