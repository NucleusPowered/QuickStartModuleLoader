/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.nontest;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

/**
 * A module that is used in tests where we're not testing the modules that have been loaded.
 */
@ModuleData(id = "fake", name = "fake")
public class FakeModule implements Module {

    public static String packageName() {
        return FakeModule.class.getPackage().getName();
    }

    @Override
    public void onEnable() {

    }
}
