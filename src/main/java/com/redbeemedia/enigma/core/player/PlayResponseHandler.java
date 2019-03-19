package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.AnonymousIpBlockedError;
import com.redbeemedia.enigma.core.error.AssetGeoBlockedError;
import com.redbeemedia.enigma.core.error.AssetNotAvailableForDeviceError;
import com.redbeemedia.enigma.core.error.AssetNotEnabledError;
import com.redbeemedia.enigma.core.error.AssetRestrictedError;
import com.redbeemedia.enigma.core.error.ExposureHttpError;
import com.redbeemedia.enigma.core.error.InternalError;
import com.redbeemedia.enigma.core.error.InvalidAssetError;
import com.redbeemedia.enigma.core.error.InvalidJsonToServerError;
import com.redbeemedia.enigma.core.error.InvalidSessionTokenError;
import com.redbeemedia.enigma.core.error.JsonResponseError;
import com.redbeemedia.enigma.core.error.LicenceExpiredError;
import com.redbeemedia.enigma.core.error.NotEntitledToAssetError;
import com.redbeemedia.enigma.core.error.ServerTimeoutError;
import com.redbeemedia.enigma.core.error.TooManyConcurrentStreamsError;
import com.redbeemedia.enigma.core.error.TooManyConcurrentSvodStreamsError;
import com.redbeemedia.enigma.core.error.TooManyConcurrentTvodStreamsError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.error.UnexpectedHttpStatusError;
import com.redbeemedia.enigma.core.error.UnknownBusinessUnitError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.json.JsonInputStreamParser;
import com.redbeemedia.enigma.core.json.JsonObjectResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/*package-protected*/ abstract class PlayResponseHandler extends JsonObjectResponseHandler {
    private String assetId;

    public PlayResponseHandler(String _assetId) {
        this.assetId = _assetId;
        IHttpCodeHandler allHttpCodeHandler = new IHttpCodeHandler() {
            @Override
            public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                String message;
                try {
                    JSONObject errorJson = JsonInputStreamParser.obtain().parse(inputStream);
                    ExposureHttpError exposureHttpError = new ExposureHttpError(errorJson);
                    message = exposureHttpError.getMessage();
                } catch (JSONException e) {
                    onError(new JsonResponseError("Exposure did not respond with error message in correct json format.", new UnexpectedError(e)));
                    return;
                }
                int statusCode = httpStatus.getResponseCode();
                if(statusCode == 400) {
                    onError(new InvalidJsonToServerError());
                } else if(statusCode == 401) {
                    onError(new InvalidSessionTokenError());
                } else if(statusCode == 403) {
                    if("NOT_AVAILABLE_IN_FORMAT".equals(message)) {
                        onError(new InternalError());
                    } else if("FORBIDDEN".equals(message)) {
                        onError(new UnknownBusinessUnitError("Server returned FORBIDDEN"));
                    } else if("NOT_ENTITLED".equals(message)) {
                        onError(new NotEntitledToAssetError());
                    } else if("DEVICE_BLOCKED".equals(message)) {
                        onError(new AssetNotAvailableForDeviceError());
                    } else if("NOT_ENABLED".equals(message)) {
                        onError(new AssetNotEnabledError());
                    } else if("GEO_BLOCKED".equals(message)) {
                        onError(new AssetGeoBlockedError());
                    } else if("LICENSE_EXPIRED".equals(message)) {
                        onError(new LicenceExpiredError());
                    } else if("CONCURRENT_STREAMS_LIMIT_REACHED".equals(message)) {
                        onError(new TooManyConcurrentStreamsError());
                    } else if("CONCURRENT_STREAMS_TVOD_LIMIT_REACHED".equals(message)) {
                        onError(new TooManyConcurrentTvodStreamsError());
                    } else if("CONCURRENT_STREAMS_SVOD_LIMIT_REACHED".equals(message)) {
                        onError(new TooManyConcurrentSvodStreamsError());
                    } else if("ANONYMOUS_IP_BLOCKED".equals(message)) {
                        onError(new AnonymousIpBlockedError());
                    } else {
                        onError(new AssetRestrictedError("message was \""+message+"\""));
                    }
                } else if(statusCode == 404) {
                    if(message.equals("UNKNOWN_BUSINESS_UNIT")) {
                        onError(new UnknownBusinessUnitError());
                    } else if(message.equals("UNKNOWN_ASSET")) {
                        onError(new InvalidAssetError(assetId));
                    } else {
                        onError(new UnexpectedHttpStatusError(new HttpStatus(statusCode, message)));
                    }
                } else if(statusCode == 422) {
                    onError(new InvalidJsonToServerError());
                } else if(statusCode == 500) {
                    onError(new ServerTimeoutError());
                } else {
                    onError(new InternalError("Got status code: "+statusCode));
                }
            }
        };
        handleErrorCode(400, allHttpCodeHandler);
        handleErrorCode(401, allHttpCodeHandler);
        handleErrorCode(403, allHttpCodeHandler);
        handleErrorCode(404, allHttpCodeHandler);
        handleErrorCode(422, allHttpCodeHandler);
        handleErrorCode(500, allHttpCodeHandler);
    }
}