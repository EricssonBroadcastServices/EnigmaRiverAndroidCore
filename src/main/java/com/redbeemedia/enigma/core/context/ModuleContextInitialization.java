// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.context;

import android.app.Application;

import java.util.Map;

/*package-protected*/ class ModuleContextInitialization implements IModuleContextInitialization {
    private final Application application;
    private final Map<String, IModuleInitializationSettings> moduleSettings;

    public ModuleContextInitialization(Application application, Map<String, IModuleInitializationSettings> moduleSettings) {
        this.application = application;
        this.moduleSettings = moduleSettings;
    }

    @Override
    public Application getApplication() {
        return application;
    }

    @Override
    public <I extends IModuleInitializationSettings> I getModuleSettings(IModuleInfo<I> moduleInfo) {
        I settings = (I) moduleSettings.get(moduleInfo.getModuleId());
        return settings != null ? settings : moduleInfo.createInitializationSettings();
    }
}
