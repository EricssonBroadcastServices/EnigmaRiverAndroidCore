// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

/**
 * Responsible for determine which <code>IAdResourceLoader</code> to be used.
 */
public interface IAdResourceLoaderFactory {

    /** Create an <code>IAdResourceLoader</code> implementatio using the ads information and a generic metadata object. */
    @Nullable <T> IAdResourceLoader createResourceLoader(@Nullable IAdMetadata adsInfo, @Nullable T metadata);

}
