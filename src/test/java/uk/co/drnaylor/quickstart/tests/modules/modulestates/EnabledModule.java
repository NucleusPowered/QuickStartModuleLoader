/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.modulestates;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "en", name = "enabled")
public class EnabledModule implements Module {

    @Override
    public void onEnable() {

    }
}
