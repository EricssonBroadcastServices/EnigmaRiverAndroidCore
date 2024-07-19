// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.epg.impl;

import android.os.Handler;

import com.redbeemedia.enigma.core.epg.IEpg;
import com.redbeemedia.enigma.core.epg.request.IEpgRequest;
import com.redbeemedia.enigma.core.epg.response.IEpgResponseHandler;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.ProxyCallback;

/*package-protected*/ abstract class AbstractEpg implements IEpg {
    @Override
    public final void getPrograms(IEpgRequest request, IEpgResponseHandler responseHandler, IHandler handler) {
        if(handler != null) {
            responseHandler = ProxyCallback.createCallbackOnThread(handler, IEpgResponseHandler.class, responseHandler);
        }
        getPrograms(request, responseHandler);
    }

    @Override
    public final void getPrograms(IEpgRequest request, IEpgResponseHandler responseHandler, Handler handler) {
        getPrograms(request, responseHandler, handler != null ? new HandlerWrapper(handler) : null);
    }
}
