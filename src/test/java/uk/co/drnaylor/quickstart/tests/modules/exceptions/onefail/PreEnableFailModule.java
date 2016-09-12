/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.exceptions.onefail;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "prefail", name = "prefail")
public class PreEnableFailModule implements Module {

    @Override
    public void preEnable() {
        throw new RuntimeException();
    }

    @Override
    public void onEnable() {

    }
}
