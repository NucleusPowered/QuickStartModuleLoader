/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.exceptions.MissingDependencyException;

import java.util.Optional;

/**
 * This class defines what a module is, what it loads, and how it works. It should be paired with the
 * {@link uk.co.drnaylor.quickstart.annotations.ModuleData} annotation in order to define metadata about it.
 */
public interface Module {

    /**
     * Gets a {@link AbstractConfigAdapter}, should one be required for the main config file.
     *
     * @return An {@link Optional} containing an {@link AbstractConfigAdapter} if the module wishes.
     */
    default Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.empty();
    }

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

    /**
     * Runs before the enable phase.
     */
    default void preEnable() {}

    /**
     * Runs when the module is enabled.
     */
    void onEnable();

    /**
     * Runs after the enable phase.
     */
    default void postEnable() {}

    /**
     * Marks a module as disableable at runtime.
     */
    interface RuntimeDisableable extends Module {

        /**
         * Runs when a module is disabled.
         */
        void onDisable();
    }
}
