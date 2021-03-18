package com.redbeemedia.enigma.core.playrequest;

import androidx.annotation.Nullable;

/**
 * Responsible for the creation <code>IAdInsertionParameters</code>.<br/>
 * This factory is typically injected into <code>EnigmaRiverContext</code> during initialization.<br/>
 * See {@link com.redbeemedia.enigma.core.context.EnigmaRiverContext.EnigmaRiverContextInitialization#setAdInsertionFactory(IAdInsertionFactory)}.
 */
public interface IAdInsertionFactory {

    /**
     * Create an <code>IAdInsertionParameters</code> container.
     * @param request Will be provided to the factory. Usage by implementor is optional.
     * @return an <code>IAdInsertionParameters</code> or <code>null</code>.
     */
    IAdInsertionParameters createParameters(@Nullable IPlayRequest request);

    /**
     * By implementing this extension of <code>IAdInsertionFactory</code> one
     * can provide the Base64 encoded string provided in <i>X-Adobe-Primetime-MediaToken</i>.
     */
    interface IAdobeAdInsertionFactory extends IAdInsertionFactory {

        static final String HTTP_HEADER_KEY = "X-Adobe-Primetime-MediaToken";

        /** Base64 encoded string provided in <i>X-Adobe-Primetime-MediaToken</i> for the play request. */
        @Nullable String getAdobePrimetimeMediaToken();

    }

}
