/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.loaders;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;

/**
 * Provides information on how to construct and enable the module classes.
 */
public interface ModuleConstructor<R extends Module> {

    /**
     * Constructs the supplied module.
     *
     * @param moduleClass The {@link Class} of the module to construct.
     * @return The {@link R}
     * @throws QuickStartModuleLoaderException.Construction if the module could not be constructed.
     */
    R constructModule(Class<? extends R> moduleClass) throws QuickStartModuleLoaderException.Construction;
}
