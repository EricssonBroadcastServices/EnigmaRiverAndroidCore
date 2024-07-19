// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.context.exception.ModuleInitializationException;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EnigmaModuleInitializerTest {
    @Test
    public void testModuleMustHaveInitializeMethod() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        try {
            List<String> modules = Arrays.asList(
                    MockModuleMissingMethod.class.getName()
            );
            EnigmaModuleInitializer.maybeInitializeModules(modules, new MockModuleContextInitialization());
            Assert.fail("Expected ModuleInitializationException");
        } catch (ModuleInitializationException e) {
            Assert.assertEquals("No static method initialize(IModuleContextInitialization)",e.getMessage().substring(0, 57));
        }
    }

    @Test
    public void testNonPublicModuleContextsCanBeInitialized() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<String> modules = Arrays.asList(
                PackageProtectedMockModule.class.getName()
        );
        int initializeCallsBefore = PackageProtectedMockModule.initializeCalls;
        try {
            EnigmaModuleInitializer.maybeInitializeModules(modules, new MockModuleContextInitialization());
        } catch (ModuleInitializationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(initializeCallsBefore+1, PackageProtectedMockModule.initializeCalls);
    }

    @Test
    public void testMultipleModuleContextInitialized() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<String> modules = Arrays.asList(
                PackageProtectedMockModule.class.getName(),
                "missing.module.NonExistentModule", // Should be allowed
                MockModule.class.getName()
        );
        int ppmmInitCalls = PackageProtectedMockModule.initializeCalls;
        int mmInitCalls = MockModule.initializeCalls;
        try {
            EnigmaModuleInitializer.maybeInitializeModules(modules, new MockModuleContextInitialization());
        } catch (ModuleInitializationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(ppmmInitCalls+1, PackageProtectedMockModule.initializeCalls);
        Assert.assertEquals(mmInitCalls+1, MockModule.initializeCalls);
    }

    @Test
    public void testModuleSettingsPassed() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<String> modules = Arrays.asList(
                ConfigurableModule.class.getName(),
                "missing.module.NonExistentModule" // Should be allowed
        );

        int cmInitCalls = ConfigurableModule.initializeCalls;
        int cmModuleInitCreations = ConfigurableModule.moduleInitializationCreations;
        try {
            EnigmaModuleInitializer.maybeInitializeModules(modules, new ModuleContextInitialization(null, new HashMap<>()));
        } catch (ModuleInitializationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(cmInitCalls+1, ConfigurableModule.initializeCalls);
        Assert.assertEquals(cmModuleInitCreations+1, ConfigurableModule.moduleInitializationCreations);
    }

    // ---- Mock module context classes

    public static class MockModuleMissingMethod {
    }

    /*package-protected*/ static class PackageProtectedMockModule {
        public static volatile int initializeCalls = 0;

        private static void initialize(IModuleContextInitialization initialization) {
            initializeCalls++;
        }
    }

    public static class MockModule {
        public static volatile int initializeCalls = 0;

        public static void initialize(IModuleContextInitialization initialization) {
            initializeCalls++;
        }
    }

    public static class ConfigurableModule {
        public static volatile int initializeCalls = 0;
        public static volatile int moduleInitializationCreations = 0;

        public static final IModuleInfo<Initialization> MODULE_INFO = new ModuleInfo<Initialization>(ConfigurableModule.class) {
            @Override
            public Initialization createInitializationSettings() {
                moduleInitializationCreations++;
                return new Initialization();
            }
        };

        public static void initialize(IModuleContextInitialization initialization) {
            initializeCalls++;
            Initialization moduleInit = initialization.getModuleSettings(MODULE_INFO);
            Assert.assertNotNull(moduleInit);
        }

        public static class Initialization implements IModuleInitializationSettings {
        }
    }
}
