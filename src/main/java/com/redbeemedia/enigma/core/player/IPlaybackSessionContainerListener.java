// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.util.IInternalListener;

/*package-protected*/ interface IPlaybackSessionContainerListener extends IInternalListener {
    void onPlaybackSessionChanged(IInternalPlaybackSession oldSession, IInternalPlaybackSession newSession);
}
