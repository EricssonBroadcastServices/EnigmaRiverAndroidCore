package com.redbeemedia.enigma.core.playrequest;

import java.util.Map;

/**
 * Represents a set of key-value pairs to be used for ad insertion typically during a play request.
 */
public interface IAdInsertionParameters {

    /**
     * @return a list of key-value pairs used for ad insertion.
     */
    Map<String,?> getParameters();

}
