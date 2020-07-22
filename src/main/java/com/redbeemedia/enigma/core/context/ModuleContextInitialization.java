package com.redbeemedia.enigma.core.context;

import android.app.Application;

/*package-protected*/ class ModuleContextInitialization implements IModuleContextInitialization {
    private final Application application;

    public ModuleContextInitialization(Application application) {
        this.application = application;
    }

    @Override
    public Application getApplication() {
        return application;
    }
}
