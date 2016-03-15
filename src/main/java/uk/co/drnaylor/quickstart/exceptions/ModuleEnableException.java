/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.exceptions;

import uk.co.drnaylor.quickstart.Module;

/**
 * Thrown if a module could not be enabled correctly.
 */
public class ModuleEnableException extends Exception {

    private final Class<? extends Module> moduleClass;

    public ModuleEnableException(Class<? extends Module> module, String message, Exception innerException) {
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
}
