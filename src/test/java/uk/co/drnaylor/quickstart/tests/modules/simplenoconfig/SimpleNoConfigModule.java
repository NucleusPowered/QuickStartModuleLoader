/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.simplenoconfig;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "simple", name = "simple")
public class SimpleNoConfigModule implements Module {
    @Override
    public void onEnable() {
        // Nothing to do.
    }
}
