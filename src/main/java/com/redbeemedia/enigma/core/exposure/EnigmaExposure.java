package com.redbeemedia.enigma.core.exposure;

import android.os.Handler;
import android.util.JsonReader;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.JsonResponseError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
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
    private IAspect aspect;
    private IHandler callbackHandler = null;

    public EnigmaExposure(ISession session) {
        this.aspect = new LoggedInAspect(session);
    }

    public EnigmaExposure(IBusinessUnit businessUnit) {
        this.aspect = new NotLoggedInAspect(businessUnit);
    }

    public EnigmaExposure setCallbackHandler(IHandler handler) {
        this.callbackHandler = handler;
        return this;
    }

    public EnigmaExposure setCallbackHandler(Handler handler) {
        return this.setCallbackHandler(new HandlerWrapper(handler));
    }

    public void doRequest(IExposureRequest<?> request) {
        aspect.doRequest(request);
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
                onError(new JsonResponseError("Failed to parse response json from Exposure.",new UnexpectedError(e)));
                return;
            } catch (Exception e) {
                onError(new UnexpectedError(e));
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

    private interface IAspect {
        void doRequest(IExposureRequest<?> request);
    }

    private abstract class BaseAspect implements IAspect {
        @Override
        public void doRequest(IExposureRequest<?> request) {
            try {
                URL url = request.getUrl(getBusinessUnit()).toURL();
                IHttpCall httpCall = getHttpCall(request);
                EnigmaRiverContext.getHttpHandler().doHttp(url, httpCall, new ExposureResponseHandler<>(request));
            } catch (MalformedURLException e) {
                request.onError(new UnexpectedError(e, "Constructed URL for http call malformed."));
            }
        }

        protected abstract IHttpCall getHttpCall(IExposureRequest<?> request);

        protected abstract IBusinessUnit getBusinessUnit();
    }

    private class LoggedInAspect extends BaseAspect {
        private ISession session;

        public LoggedInAspect(ISession session) {
            this.session = session;
        }

        @Override
        protected IHttpCall getHttpCall(IExposureRequest<?> request) {
            return request.getHttpCall(session);
        }

        @Override
        protected IBusinessUnit getBusinessUnit() {
            return session.getBusinessUnit();
        }
    }

    private class NotLoggedInAspect extends BaseAspect {
        private IBusinessUnit businessUnit;

        public NotLoggedInAspect(IBusinessUnit businessUnit) {
            this.businessUnit = businessUnit;
        }

        @Override
        protected IHttpCall getHttpCall(IExposureRequest<?> request) {
            return request.getHttpCall(businessUnit);
        }

        @Override
        protected IBusinessUnit getBusinessUnit() {
            return businessUnit;
        }
    }
}
