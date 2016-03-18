/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.modulestates;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

@ModuleData(id = "dis", name = "disabled", status = LoadingStatus.DISABLED)
public class DisabledModule implements Module {
    @Override
    public void onEnable() {

    }
}
