/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import uk.co.drnaylor.quickstart.exceptions.MissingDependencyException;

/**
 * This class defines what a module is, what it loads, and how it works. It should be paired with the
 * {@link uk.co.drnaylor.quickstart.annotations.ModuleData} annotation in order to define metadata about it.
 */
public interface Module {

    /**
     * Performs additional checks to ensure the module has everything it requires loaded.
     *
     * <p>
     *     If a dependency check fails, modules should throw a {@link MissingDependencyException}
     *     along with a suitable message.
     * </p>
     *
     * @throws MissingDependencyException thrown if a dependency is missing
     */
    default void checkExternalDependencies() throws MissingDependencyException {}

}
