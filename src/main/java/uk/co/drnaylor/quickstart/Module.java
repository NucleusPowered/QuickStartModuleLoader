/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.util.Optional;

public interface Module {

    default Optional<AbstractConfigAdapter> getConfigAdapter() {
        return Optional.empty();
    }

    void onEnable();
}
