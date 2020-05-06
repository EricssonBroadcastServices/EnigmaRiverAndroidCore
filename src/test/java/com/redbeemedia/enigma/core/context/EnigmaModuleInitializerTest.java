package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.context.exception.ModuleInitializationException;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class EnigmaModuleInitializerTest {
    @Test
    public void testModuleMustHaveInitializeMethod() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        try {
            List<String> modules = Arrays.asList(
                    MockModuleMissingMethod.class.getName()
            );
            EnigmaModuleInitializer.maybeInitializeModules(modules);
            Assert.fail("Expected ModuleInitializationException");
        } catch (ModuleInitializationException e) {
            Assert.assertEquals("No static method called initialize in",e.getMessage().substring(0, 37));
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
            EnigmaModuleInitializer.maybeInitializeModules(modules);
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
            EnigmaModuleInitializer.maybeInitializeModules(modules);
        } catch (ModuleInitializationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(ppmmInitCalls+1, PackageProtectedMockModule.initializeCalls);
        Assert.assertEquals(mmInitCalls+1, MockModule.initializeCalls);
    }

    // ---- Mock module context classes

    public static class MockModuleMissingMethod {
    }

    /*package-protected*/ static class PackageProtectedMockModule {
        public static volatile int initializeCalls = 0;

        private static void initialize() {
            initializeCalls++;
        }
    }

    public static class MockModule {
        public static volatile int initializeCalls = 0;

        public static void initialize() {
            initializeCalls++;
        }
    }
}
