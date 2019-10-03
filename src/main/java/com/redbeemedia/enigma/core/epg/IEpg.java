package com.redbeemedia.enigma.core.epg;

import android.os.Handler;

import com.redbeemedia.enigma.core.epg.request.IEpgRequest;
import com.redbeemedia.enigma.core.epg.response.IEpgResponseHandler;
import com.redbeemedia.enigma.core.util.IHandler;

public interface IEpg {
    void getPrograms(IEpgRequest request, IEpgResponseHandler responseHandler);
    void getPrograms(IEpgRequest request, IEpgResponseHandler responseHandler, IHandler handler);
    void getPrograms(IEpgRequest request, IEpgResponseHandler responseHandler, Handler handler);
}
