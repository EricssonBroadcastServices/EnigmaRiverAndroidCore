package com.redbeemedia.enigma.core.restriction;

public interface IContractRestrictions {
    /**
     * Tries to return a non-<code>null</code> value for the contract restriction using the following priority:
     * <ol>
     *     <li>Value defined in play-response</li>
     *     <li>System default value</li>
     *     <li><code>fallback</code></li>
     * </ol>
     *
     * @param restriction See {@link ContractRestriction}
     * @param fallback Used if no value, not default value was present.
     * @param <T>
     * @return
     */
    <T> T getValue(IContractRestriction<T> restriction, T fallback);
}
