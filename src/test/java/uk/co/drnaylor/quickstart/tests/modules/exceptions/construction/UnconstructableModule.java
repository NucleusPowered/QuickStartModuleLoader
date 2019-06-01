/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.exceptions.construction;

import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.tests.modules.TestModule;

@ModuleData(id = "construct", name = "construct")
public class UnconstructableModule implements TestModule {

    public UnconstructableModule() {
        throw new RuntimeException();
    }

    @Override
    public void onEnable() {

    }
}
