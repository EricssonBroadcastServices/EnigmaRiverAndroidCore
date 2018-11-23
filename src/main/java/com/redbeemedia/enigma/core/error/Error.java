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
    public static final Error UNKNOWN_ASSET = new Error(ErrorType.NetworkError,"The asset was not found");
    public static final Error UNEXPECTED_ERROR = new Error(ErrorType.UnknownError,"Something went wrong");

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

   public enum ErrorType {
        NetworkError,
        JsonError,
       UnknownError,
        //TODO: add error types
    }

}