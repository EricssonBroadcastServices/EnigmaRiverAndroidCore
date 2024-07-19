// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.IInternalCallbackObject;

public interface ILoginResultHandler extends IInternalCallbackObject {
    void onSuccess(ISession session);
    void onError(EnigmaError error);
}
