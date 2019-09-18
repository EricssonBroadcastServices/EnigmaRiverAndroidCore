package com.redbeemedia.enigma.core.restriction;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public interface IContractRestrictionsValueSource {
    <T> boolean hasValue(String name, Class<T> type);
    <T> T getValue(String name, Class<T> type);
}
