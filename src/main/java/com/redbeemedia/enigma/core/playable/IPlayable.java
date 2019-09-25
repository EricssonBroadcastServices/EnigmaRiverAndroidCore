package com.redbeemedia.enigma.core.playable;

import android.os.Parcelable;

public interface IPlayable extends Parcelable {
    void useWith(IPlayableHandler playableHandler);
}
