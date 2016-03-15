/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.exceptions;

import uk.co.drnaylor.quickstart.Module;

public abstract class QuickStartModuleLoaderException extends Exception {

    private final Class<? extends Module> moduleClass;

    public QuickStartModuleLoaderException(Class<? extends Module> module, String message, Exception innerException) {
        super(message, innerException);
        this.moduleClass = module;
    }

    /**
     * Gets the {@link Class} of the module that failed to load.
     *
     * @return The {@link Class}
     */
    public Class<? extends Module> getModuleClass() {
        return this.moduleClass;
    }

    /**
     * Failure during Module Construction
     */
    public static class Construction extends QuickStartModuleLoaderException {

        public Construction(Class<? extends Module> module, String message, Exception innerException) {
            super(module, message, innerException);
        }
    }

    /**
     * Failure during Module Discovery
     */
    public static class Discovery extends QuickStartModuleLoaderException {

        public Discovery(Class<? extends Module> module, String message, Exception innerException) {
            super(module, message, innerException);
        }
    }

    /**
     * Failure during Module Enabling
     */
    public static class Enabling extends QuickStartModuleLoaderException {

        public Enabling(Class<? extends Module> module, String message, Exception innerException) {
            super(module, message, innerException);
        }
    }
}
