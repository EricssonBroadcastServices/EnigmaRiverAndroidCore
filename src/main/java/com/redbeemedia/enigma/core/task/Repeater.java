package com.redbeemedia.enigma.core.task;

/**
 * <p>
 * This class executes a runnable every <code>delayMillis</code> milliseconds while it is enabled.
 * The {@link Repeater} starts disabled.
 * </p>
 * <p>
 * Calling <code>executeNow()</code> will always result in an immediate
 * call of <code>runnable.run()</code>. If enabled, the next call to <code>runnable.run()</code>
 * will be triggered after <code>delayMillis</code> milliseconds.
 * </p>
 */
public class Repeater {
    private static volatile IRepeaterImplementation.Factory implementationFactory = new IRepeaterImplementation.Factory() {
        @Override
        public IRepeaterImplementation create(ITaskFactory taskFactory, long delayMillis, Runnable runnable) {
            return new RepeaterImplementation(taskFactory, delayMillis, runnable);
        }
    };

    public static void setImplementation(IRepeaterImplementation.Factory factory) {
        implementationFactory = factory;
    }

    private final IRepeaterImplementation implementation;

    public Repeater(ITaskFactory taskFactory, long delayMillis, Runnable runnable) {
        this.implementation = implementationFactory.create(taskFactory, delayMillis, runnable);
    }

    public void setEnabled(boolean enabled) {
        implementation.setEnabled(enabled);
    }

    public void executeNow() {
        implementation.executeNow();
    }
}
