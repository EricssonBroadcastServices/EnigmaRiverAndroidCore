package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.context.exception.ModuleInitializationException;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.ReflectionUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ModuleTests {

    @Test
    public void testDefaultModuleSettingsCreated() throws ModuleInitializationException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        EnigmaRiverContext.EnigmaRiverContextInitialization initialization
                = new EnigmaRiverContext.EnigmaRiverContextInitialization("http://example.com/mockBase");

        List<String> modules = Arrays.asList(
                MockModule.class.getName(),
                "missing.module.NonExistentModule" // Should be allowed
        );

        final Counter createInitializationCalls = new Counter();
        final Counter initializeCalls = new Counter();

        MockModule.MODULE_INFO = new ModuleInfo<MockModule.Initialization>(MockModule.class) {
            @Override
            public MockModule.Initialization createInitializationSettings() {
                createInitializationCalls.count();
                return new MockModule.Initialization();
            }
        };
        MockModule.initializeMethod = new MockModule.IInitializeMethod() {
            @Override
            public void initialize(IModuleContextInitialization initialization) {
                initializeCalls.count();
                MockModule.Initialization moduleInitialization = initialization.getModuleSettings(MockModule.MODULE_INFO);
                Assert.assertNotNull(moduleInitialization);
            }
        };

        createInitializationCalls.assertNone();
        initializeCalls.assertNone();

        Map moduleSettings = ReflectionUtil.getDeclaredField(initialization, Map.class, "moduleSettings");
        ModuleContextInitialization moduleContextInitialization = new ModuleContextInitialization(null, moduleSettings);
        EnigmaModuleInitializer.maybeInitializeModules(modules, moduleContextInitialization);

        createInitializationCalls.assertOnce();
        initializeCalls.assertOnce();
    }

    @Test
    public void testModuleSettingsCanBeEdited() throws ModuleInitializationException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        EnigmaRiverContext.EnigmaRiverContextInitialization initialization
                = new EnigmaRiverContext.EnigmaRiverContextInitialization("http://example.com/mockBase");

        List<String> modules = Arrays.asList(
                MockModule.class.getName(),
                "missing.module.NonExistentModule" // Should be allowed
        );

        final Counter createInitializationCalls = new Counter();
        final Counter initializeCalls = new Counter();

        MockModule.MODULE_INFO = new ModuleInfo<MockModule.Initialization>(MockModule.class) {
            @Override
            public MockModule.Initialization createInitializationSettings() {
                createInitializationCalls.count();
                return new MockModule.Initialization();
            }
        };
        MockModule.initializeMethod = new MockModule.IInitializeMethod() {
            @Override
            public void initialize(IModuleContextInitialization initialization) {
                initializeCalls.count();
                MockModule.Initialization moduleSettings = initialization.getModuleSettings(MockModule.MODULE_INFO);
                Assert.assertNotNull(moduleSettings);
                Assert.assertEquals("Test data", moduleSettings.data);
            }
        };

        createInitializationCalls.assertNone();
        initializeCalls.assertNone();

        MockModule.Initialization mockModuleInit = initialization.forModule(MockModule.MODULE_INFO);
        Assert.assertNotNull(mockModuleInit);

        createInitializationCalls.assertOnce();
        initializeCalls.assertNone();

        mockModuleInit.data = "Test data";

        Assert.assertSame(mockModuleInit, initialization.forModule(MockModule.MODULE_INFO));

        createInitializationCalls.assertOnce();
        initializeCalls.assertNone();

        Map moduleSettings = ReflectionUtil.getDeclaredField(initialization, Map.class, "moduleSettings");
        ModuleContextInitialization moduleContextInitialization = new ModuleContextInitialization(null, moduleSettings);
        EnigmaModuleInitializer.maybeInitializeModules(modules, moduleContextInitialization);

        createInitializationCalls.assertOnce();
        initializeCalls.assertOnce();
    }

    private static class MockModule {
        public static IModuleInfo<MockModule.Initialization> MODULE_INFO = null;
        public static IInitializeMethod initializeMethod = null;

        private static class Initialization implements IModuleInitializationSettings {
            private String data = null;

        }

        private static void initialize(IModuleContextInitialization initialization) {
            initializeMethod.initialize(initialization);
        }

        private interface IInitializeMethod {
            void initialize(IModuleContextInitialization initialization);
        }
    }
}
