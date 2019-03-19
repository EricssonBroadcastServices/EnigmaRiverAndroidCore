package com.redbeemedia.enigma.core.exposure;

import android.util.JsonReader;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.UrlPath;

public interface IExposureRequest<SuccessT> {
    void onSuccess(SuccessT obj);
    void onError(Error error);

    UrlPath getUrl(IBusinessUnit businessUnit);
    IHttpCall getHttpCall(ISession session);
    IHttpCall getHttpCall(IBusinessUnit businessUnit);
    IJsonParsingMethod<SuccessT> getJsonParsingMethod();

    interface IJsonParsingMethod<T> {
        T parse(JsonReader jsonReader) throws Exception;
    }
}
