/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.simpleconfig;

import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.tests.modules.TestModule;

import java.util.Optional;

@ModuleData(id = "simpleconfig", name = "simpleconfig")
public class SimpleConfigModule implements TestModule {

    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.of(new SimpleNodeConfigAdapter());
    }

    @Override
    public void onEnable() {

    }
}
