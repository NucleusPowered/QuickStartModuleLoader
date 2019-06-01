/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules;

import uk.co.drnaylor.quickstart.Module;

public interface TestModule extends Module {

    default void preEnable() { }

    void onEnable();

    default void postEnable() { }

}
