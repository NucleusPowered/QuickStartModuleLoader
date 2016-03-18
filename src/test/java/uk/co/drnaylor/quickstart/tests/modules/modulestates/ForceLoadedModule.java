/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.modulestates;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

@ModuleData(id = "fl", name = "forceload", status = LoadingStatus.FORCELOAD)
public class ForceLoadedModule implements Module {
    @Override
    public void onEnable() {

    }
}
