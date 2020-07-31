package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.context.exception.ModuleInitializationException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*package-protected*/ class EnigmaModuleInitializer {
    private static final String INITIALIZE_METHOD_NAME = "initialize";

    /**
     *  Names of supported modules that are part of the Enigma River Android SDK.
     */
    private static final List<String> supportedModuleNames = buildSupported()
            .add("com.redbeemedia.enigma.exoplayerintegration.ExoPlayerIntegrationContext")
            .add("com.redbeemedia.enigma.download.EnigmaDownloadContext")
            .add("com.redbeemedia.enigma.exoplayerdownload.ExoPlayerDownloadContext")
            .build();

    /*package-protected*/ static void initializeModules(IModuleContextInitialization initialization) throws ModuleInitializationException {
        maybeInitializeModules(supportedModuleNames, initialization);
    }

    /*package-protected*/ static void maybeInitializeModules(List<String> moduleNames, IModuleContextInitialization initialization) throws ModuleInitializationException {
        for(String moduleName : moduleNames) {
            maybeInitializeModule(moduleName, initialization);
        }
    }

    /**
     * Tries to initialize the module if a class is found by the
     * {@code moduleName}
     * @param moduleName QName of module context class (see {@link #supportedModuleNames})
     */
    private static void maybeInitializeModule(String moduleName, IModuleContextInitialization initialization) throws ModuleInitializationException {
        Class<?> moduleContextClass;
        try {
            moduleContextClass = Class.forName(moduleName);
        } catch (ClassNotFoundException e) {
            // No class found by that name in project
            return;
        }
        initializeModule(moduleName, moduleContextClass, initialization);
    }

    private static void initializeModule(String moduleName, Class<?> moduleContextClass, IModuleContextInitialization initialization) throws ModuleInitializationException {
        Method initializerMethod = findInitializerMethod(moduleContextClass);
        if(initializerMethod == null) {
            throw new ModuleInitializationException("No static method "+INITIALIZE_METHOD_NAME+"("+IModuleContextInitialization.class.getSimpleName()+") found in "+moduleName);
        }
        withAccess(initializerMethod, (IMethodOperation<Void>) method -> {
            try {
                method.invoke(null, initialization);
            } catch (Exception e) {
                throw new ModuleInitializationException("Failed to initialize module "+moduleName,e);
            }
            return null;
        });

    }

    private static Method findInitializerMethod(Class<?> moduleContextClass) throws ModuleInitializationException {
        IMethodOperation<Boolean> isInitializer = new IsInitializerChecker();
        for(Method declaredMethod : moduleContextClass.getDeclaredMethods()) {
            if(withAccess(declaredMethod, isInitializer)) {
                return declaredMethod;
            }
        }
        return null;
    }

    /**
     * Sets the method to accessible for the duration of the {@code methodOperation} and then
     * resets to previous accessibility state.
     *
     * @param method Method to access
     * @param methodOperation Code block wherein method is accessible
     * @param <T> Return type of operation
     * @return
     * @throws ModuleInitializationException
     */
    private static <T> T withAccess(Method method, IMethodOperation<T> methodOperation) throws ModuleInitializationException {
        boolean beforeOperation = method.isAccessible();
        try {
            method.setAccessible(true);
            return methodOperation.execute(method);
        } finally {
            method.setAccessible(beforeOperation);
        }
    }

    private interface IMethodOperation<T> {
        T execute(Method method) throws ModuleInitializationException;
    }

    private static class IsInitializerChecker implements IMethodOperation<Boolean> {
        @Override
        public Boolean execute(Method method) {
            return isStatic(method)
                && hasCorrectName(method)
                && hasCorrectParameters(method);
        }

        private boolean isStatic(Method method) {
            return Modifier.isStatic(method.getModifiers());
        }

        private boolean hasCorrectName(Method method) {
            return method.getName().equals(INITIALIZE_METHOD_NAME);
        }

        private boolean hasCorrectParameters(Method method) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if(parameterTypes.length != 1) {
                return false;
            } else {
                return parameterTypes[0].isAssignableFrom(IModuleContextInitialization.class);
            }
        }
    }

    private static ImmutableListBuilder<String> buildSupported() {
        return new ImmutableListBuilder<>();
    }

    private static class ImmutableListBuilder<T> {
        private final List<T> list = new ArrayList<>();

        public ImmutableListBuilder<T> add(T obj) {
            list.add(obj);
            return this;
        }

        public List<T> build() {
            return Collections.unmodifiableList(new ArrayList<>(list));
        }
    }
}
