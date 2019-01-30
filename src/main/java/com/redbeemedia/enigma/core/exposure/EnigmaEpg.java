package com.redbeemedia.enigma.core.exposure;

import android.util.JsonReader;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.exposure.models.channel.ApiChannelEPGResponse;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.json.JsonReaderResponseHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class EnigmaEpg {
    private ISession session;

    public EnigmaEpg(ISession session) {
        this.session = session;
    }

    public void getEpgData(IGetEpgDataRequest getEpgDataRequest) { //TODO just have one method and take in different concrete types on IEpgRequest!
        try {
            URL url = getEpgDataRequest.appendQueryParameters(session.getApiBaseUrl().append("epg")).toURL();
            IHttpCall httpCall = new AuthenticatedExposureApiCall("GET", session);
            IHttpHandler.IHttpResponseHandler httpResponseHandler = new ApiGetEpgDataResponseHandler(getEpgDataRequest);

            EnigmaRiverContext.getHttpHandler().doHttp(url, httpCall, httpResponseHandler);
        } catch (MalformedURLException e) {
            IGetEpgDataResultHandler resultHandler = getEpgDataRequest.getResultHandler();
            resultHandler.onError(Error.UNKNOWN_BUSINESS_UNIT);
        }
    }

    private static class ApiGetEpgDataResponseHandler extends JsonReaderResponseHandler {
        private IGetEpgDataRequest getEpgDataRequest;

        public ApiGetEpgDataResponseHandler(IGetEpgDataRequest getEpgDataRequest) {
            this.getEpgDataRequest = getEpgDataRequest;
        }

        @Override
        protected void onError(Error error) {
            IGetEpgDataResultHandler resultHandler = getEpgDataRequest.getResultHandler();
            resultHandler.onError(error);
        }

        @Override
        protected void onSuccess(JsonReader jsonReader) {
            try {
                List<ApiChannelEPGResponse> response = JsonReaderUtil.readArray(jsonReader, ApiChannelEPGResponse.class);
                IGetEpgDataResultHandler resultHandler = getEpgDataRequest.getResultHandler();
                resultHandler.onSuccess(response);
            } catch (IOException e) {
                onError(Error.FAILED_TO_PARSE_RESPONSE_JSON);
            }
        }
    }
}
