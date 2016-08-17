/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.loaders;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;

/**
 * The {@link ModuleEnabler} contains the common logic for enabling all modules.
 *
 * <p>
 *     Usually, all that this needs to do is run the respective {@link Module} enabling methods, but there may be times
 *     where checks or changes need to be performed to all modules. The Enabler can simplify this task.
 * </p>
 *
 * <p>
 *     Most cases should not need to use this.
 * </p>
 */
public interface ModuleEnabler {

    /**
     * The default {@link ModuleEnabler}.
     */
    ModuleEnabler SIMPLE_INSTANCE = new ModuleEnabler() {};

    /**
     * Prepares modules for being enabled.
     *
     * @param module The {@link Module} to enable.
     * @throws QuickStartModuleLoaderException.Enabling if the module could not be pre-enabled.
     */
    default void preEnableModule(Module module) throws QuickStartModuleLoaderException.Enabling {
        try {
            module.preEnable();
        } catch (Exception e) {
            throw new QuickStartModuleLoaderException.Enabling(module.getClass(), "Unable to enable the module: " + module.getClass().getName(), e);
        }
    }

    /**
     * Enables the supplied module.
     *
     * @param module The {@link Module} to enable.
     * @throws QuickStartModuleLoaderException.Enabling if the module could not be enabled.
     */
    default void enableModule(Module module) throws QuickStartModuleLoaderException.Enabling {
        try {
            module.onEnable();
        } catch (Exception e) {
            throw new QuickStartModuleLoaderException.Enabling(module.getClass(), "Unable to enable the module: " + module.getClass().getName(), e);
        }
    }

    /**
     * Performs final tasks for after modules are enabled.
     *
     * @param module The {@link Module} to enable.
     * @throws QuickStartModuleLoaderException.Enabling if the module could not be pre-enabled.
     */
    default void postEnableModule(Module module) throws QuickStartModuleLoaderException.Enabling {
        try {
            module.postEnable();
        } catch (Exception e) {
            throw new QuickStartModuleLoaderException.Enabling(module.getClass(), "Unable to enable the module: " + module.getClass().getName(), e);
        }
    }
}
