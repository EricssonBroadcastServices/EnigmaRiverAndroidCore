// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import com.redbeemedia.enigma.core.playrequest.IPlayRequest;

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

}
