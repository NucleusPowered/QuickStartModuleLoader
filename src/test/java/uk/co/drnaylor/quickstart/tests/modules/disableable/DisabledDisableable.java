/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.disableable;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

@ModuleData(id = "disdis", name = "DisDis", status = LoadingStatus.DISABLED)
public class DisabledDisableable implements Module.RuntimeDisableable {

    @Override public void onEnable() {

    }

    @Override public void onDisable() {

    }
}
