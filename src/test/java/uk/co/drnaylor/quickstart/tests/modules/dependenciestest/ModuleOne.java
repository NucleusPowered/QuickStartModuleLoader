/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.dependenciestest;

import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.config.SimpleNodeConfigAdapter;
import uk.co.drnaylor.quickstart.tests.modules.TestModule;

import java.util.Optional;

@ModuleData(id = "moduleone", name = "moduleone")
public class ModuleOne implements TestModule {

    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.of(new SimpleNodeConfigAdapter());
    }

    @Override
    public void onEnable() {

    }
}
