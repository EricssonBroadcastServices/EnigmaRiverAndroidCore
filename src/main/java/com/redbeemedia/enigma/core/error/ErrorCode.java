package com.redbeemedia.enigma.core.error;

public class ErrorCode {
    private static final int EXO_PLAYER_INTERNAL_ERROR = 1; //From old SDK
    private static final int EXPOSURE_ENTITLEMENT_ERROR = 2; //From old SDK
    private static final int DOWNLOAD_RUNTIME_ERROR = 4; //From old SDK
    private static final int DOWNLOAD_ENTITLEMENT_ERROR = 5; //From old SDK
    private static final int DOWNLOAD_MANIFEST_FAILIURE = 6; //From old SDK
    private static final int DOWNLOAD_SEGMENT_FAILED = 7; //From old SDK
    private static final int GENERIC_PLAYBACK_FAILED = 8; //From old SDK
    private static final int PLAYBACK_NOT_ENTITLED = 9; //From old SDK
    private static final int PLAYBACK_PROGRAM_NOT_FOUND = 10; //From old SDK
    private static final int EXPOSURE_DOWN = 11; //From old SDK
    private static final int INVALID_MANIFEST = 12; //From old SDK
    public static final int NETWORK_ERROR = 13; //From old SDK
    public static final int DEVICE_LIMIT_REACHED = 14;
    public static final int SESSION_LIMIT_EXCEEDED = 15;
    public static final int UNKNOWN_DEVICE_ID = 16;
    public static final int INVALID_JSON = 17;
    public static final int INVALID_JSON_RESPONSE = 18;
    public static final int UNEXPECTED = 19;
    public static final int EMPTY_RESPONSE = 20;
    public static final int INVALID_CREDENTIALS = 21;
    public static final int INVALID_SESSION_TOKEN = 22;
    public static final int UNSUPPORTED_STREAM_FORMAT = 23;
    public static final int UNKNOWN_BUSINESS_UNIT = 24;
    public static final int DEVICE_DENIED = 25;
    public static final int SERVER_ERROR = 26;
    public static final int INTERNAL_ERROR = 27;
    public static final int INVALID_ASSET = 28;
    public static final int NOT_ENTITLED = 29;
    public static final int NOT_ENABLED = 30;
    public static final int TOO_MANY_CONCURRENT_STREAMS = 31;
    public static final int TOO_MANY_CONCURRENT_TVODS = 32;
    public static final int TOO_MANY_CONCURRENT_SVODS = 33;
    public static final int GEO_BLOCKED = 34;
    public static final int ANONYMOUS_IP_BLOCKED = 35;
    public static final int ASSET_RESTRICTED = 36;
    public static final int LICENCE_EXPIRED = 37;
    public static final int NO_SUPPORTED_MEDIAFORMAT_FOUND = 38;
    public static final int SERVER_TIMEOUT = 39;
    public static final int PLAYER_IMPLEMENTATION_ERROR = 40;
    public static final int ANALYTICS_ERROR = 41;
    public static final int ILLEGAL_SEEK_POSITION = 42;
    public static final int ASSET_BLOCKED = 43;
    public static final int ASSET_NOT_AVAILABLE = 44;
    public static final int ASSET_NOT_PUBLISHED = 45;
    public static final int DRM_KEYS_EXPIRED = 46;
    public static final int MAX_DOWNLOAD_COUNT_LIMIT_REACHED = 47;


    private ErrorCode() {}
}
