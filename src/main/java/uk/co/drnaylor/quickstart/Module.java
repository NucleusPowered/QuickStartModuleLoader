/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

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
     * Runs when the module is enabled.
     */
    void onEnable();
}
