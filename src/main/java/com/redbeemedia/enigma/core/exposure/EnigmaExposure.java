package com.redbeemedia.enigma.core.exposure;

import android.os.Handler;
import android.util.JsonReader;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.json.JsonReaderResponseHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.ProxyCallback;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class EnigmaExposure {
    private ISession session;
    private IHandler callbackHandler = null;

    public EnigmaExposure(ISession session) {
        this.session = session;
    }

    public EnigmaExposure setCallbackHandler(IHandler handler) {
        this.callbackHandler = handler;
        return this;
    }

    public EnigmaExposure setCallbackHandler(Handler handler) {
        return this.setCallbackHandler(new HandlerWrapper(handler));
    }

    public void doRequest(IExposureRequest<?> request) {
        try {
            URL url = request.getUrl(session).toURL();
            IHttpCall httpCall = request.getHttpCall(session);
            EnigmaRiverContext.getHttpHandler().doHttp(url, httpCall, new ExposureResponseHandler<>(request));
        } catch (MalformedURLException e) {
            request.onError(Error.UNEXPECTED_ERROR);
        }
    }

    private class ExposureResponseHandler<SuccessT> extends JsonReaderResponseHandler {
        private IExposureRequest<SuccessT> request;

        public ExposureResponseHandler(IExposureRequest<SuccessT> request) {
            this.request = request;
        }

        @Override
        protected void onSuccess(JsonReader jsonReader) {
            IExposureRequest.IJsonParsingMethod<SuccessT> parsingMethod = request.getJsonParsingMethod();
            SuccessT successObject;
            try {
                successObject = parsingMethod.parse(jsonReader);
            } catch (IOException e) {
                onError(Error.FAILED_TO_PARSE_RESPONSE_JSON);
                return;
            } catch (Exception e) {
                onError(Error.UNEXPECTED_ERROR);
                return;
            }
            getExposureRequestCallback().onSuccess(successObject);
        }

        @Override
        protected void onError(Error error) {
            getExposureRequestCallback().onError(error);
        }

        private IExposureRequest<SuccessT> getExposureRequestCallback() {
            if(callbackHandler != null) {
                return ProxyCallback.createCallbackOnThread(callbackHandler, IExposureRequest.class, request);
            } else {
                return request;
            }
        }
    }
}
