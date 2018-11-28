package com.redbeemedia.enigma.core.error;

public class Error {

    public static final Error DEVICE_LIMIT_EXCEEDED = new Error(ErrorType.NetworkError,"The account is not allowed to register more devices");
    public static final Error SESSION_LIMIT_EXCEEDED = new Error(ErrorType.NetworkError,"The account has maximum allowed sessions");
    public static final Error UNKNOWN_DEVICE_ID = new Error(ErrorType.NetworkError,"Device body is missing or the device ID is not found");
    public static final Error INVALID_JSON = new Error(ErrorType.NetworkError,"Received JSON is not valid");
    public static final Error THIRD_PARTY_ERROR = new Error(ErrorType.NetworkError,"Third party login generate error message, for detail error code see field extendedMessage");

    public static final Error FAILED_TO_PARSE_RESPONSE_JSON = new Error(ErrorType.NetworkError,"Failed to parse json");
    public static final Error EMPTY_RESPONSE = new Error(ErrorType.UnknownError,"Empty response");
    public static final Error NETWORK_ERROR = new Error(ErrorType.NetworkError,"Network error");
    public static final Error INCORRECT_CREDENTIALS = new Error(ErrorType.NetworkError,"Underlying CRM does not accept the given credentials");
    public static final Error UNKNOWN_BUSINESS_UNIT = new Error(ErrorType.NetworkError,null);
    public static final Error UNEXPECTED_ERROR = new Error(ErrorType.UnknownError,"Something went wrong");
    public static final Error TODO = new Error(ErrorType.UnknownError, "PLACEHOLDER ERROR"); //TODO remove
    public static final Error INVALID_SESSION = new Error(ErrorType.EnigmaError, null); //TODO remove messages
    public static final Error NOT_ENTITLED = new Error(ErrorType.EntitlementError, null) ;
    public static final Error DEVICE_BLOCKED = new Error(ErrorType.EntitlementError, null);
    public static final Error GEO_BLOCKED = new Error(ErrorType.EntitlementError, null);
    public static final Error ANONYMOUS_IP_BLOCKED = new Error(ErrorType.EntitlementError, null);
    public static final Error EXPIRED_ASSET = new Error(ErrorType.AssetError, null);
    public static final Error NOT_ENABLED = new Error(ErrorType.AssetError, null);
    public static final Error TOO_MANY_CONCURRENT_STREAMS = new Error(ErrorType.ConcurrentPlaysError, null);
    public static final Error TOO_MANY_CONCURRENT_TVODS = new Error(ErrorType.ConcurrentPlaysError, null);
    public static final Error TOO_MANY_CONCURRENT_SVODS = new Error(ErrorType.ConcurrentPlaysError, null);

    private String message;
    private ErrorType errorType;

    public Error(ErrorType errorType, String message) {
        this.errorType = errorType;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public ErrorType getErrorType() { return errorType; }

   public enum ErrorType { //TODO have better types
        NetworkError,
        JsonError,
       UnknownError,
       EnigmaError, //Error related to Enigma logic
       EntitlementError, //Error related to not being able to plat due to insufficient entitlement.
       AssetError, //Error related to that an asset is not playable (for any users)
       ConcurrentPlaysError //Error related to that there are too many concurrent medias playing for an account
        //TODO: add error types
    }

}
