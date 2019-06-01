/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.modulestates;

import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.tests.modules.TestModule;

@ModuleData(id = "man", name = "mandat", isRequired = true)
public class MandatoryModule implements TestModule {
    @Override
    public void onEnable() {

    }
}
