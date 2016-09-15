/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.adapterstransformtest;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.tests.config.adapters.SimpleWithDefaultAndTransform;

import java.util.Optional;

@ModuleData(id = "moduleone", name = "moduleone")
public class ModuleOne implements Module {

    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.of(new SimpleWithDefaultAndTransform());
    }

    @Override
    public void onEnable() {

    }
}
