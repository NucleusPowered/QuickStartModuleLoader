/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.disableable;

import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.tests.modules.DisableableModule;

@ModuleData(id = "dis", name = "Dis")
public class Disableable implements DisableableModule {

    public void onEnable() {

    }

    public void onDisable() {

    }
}
