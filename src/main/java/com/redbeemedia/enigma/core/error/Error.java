package com.redbeemedia.enigma.core.error;

public class Error {

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