/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.disableable;

import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;
import uk.co.drnaylor.quickstart.tests.modules.TestModule;

@ModuleData(id = "disnotdis", name = "Dis Not Dis", status = LoadingStatus.DISABLED)
public class DisabledNotDisableable implements TestModule {

    @Override public void onEnable() {

    }
}
