// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.context;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public abstract class ModuleInfo<I extends IModuleInitializationSettings> implements IModuleInfo<I> {
    private final String moduleId;

    public ModuleInfo(Class<?> moduleClass) {
        this(moduleClass.getName());
    }

    public ModuleInfo(String moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public String getModuleId() {
        return moduleId;
    }
}
