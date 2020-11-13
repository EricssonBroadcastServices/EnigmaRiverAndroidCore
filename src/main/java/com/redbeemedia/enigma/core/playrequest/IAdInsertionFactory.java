package com.redbeemedia.enigma.core.playrequest;

/**
 * Responsible for the creation <code>IAdInsertionParameters</code>.<br/>
 * This factory is typically injected into <code>EnigmaRiverContext</code> during initialization.<br/>
 * See {@link com.redbeemedia.enigma.core.context.EnigmaRiverContext.EnigmaRiverContextInitialization#setAdInsertionFactory(IAdInsertionFactory)}.
 */
public interface IAdInsertionFactory {

    /**
     * Create an <code>IAdInsertionParameters</code> container.
     * @param request
     * @return an <code>IAdInsertionParameters</code> or <code>null</code>.
     */
    IAdInsertionParameters createParameters(IPlayRequest request);

}
