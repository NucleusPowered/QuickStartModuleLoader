/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import org.junit.Assert;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.exceptions.UndisableableModuleException;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

public class DisableableModuleTests extends FakeLoaderTests {

    @Test
    public void testThatRuntimeDisableableModuleCanBeDisabled() throws Exception {
        ModuleContainer container = getContainer("uk.co.drnaylor.quickstart.tests.modules.disableable");
        container.loadModules(true);

        Assert.assertTrue(container.isModuleLoaded("dis"));
        container.disableModule("dis");
        Assert.assertFalse(container.isModuleLoaded("dis"));
    }

    @Test(expected = UndisableableModuleException.class)
    public void testThatStandardModuleCannotBeDisabled() throws Exception {
        ModuleContainer container = getContainer("uk.co.drnaylor.quickstart.tests.modules.disableable");
        container.loadModules(true);

        container.disableModule("notdis");
    }

    @Test
    public void testThatRuntimeDisableableModuleCanBeEnabledAtRuntime() throws Exception {
        ModuleContainer container = getContainer("uk.co.drnaylor.quickstart.tests.modules.disableable");
        container.loadModules(true);

        Assert.assertFalse(container.isModuleLoaded("disdis"));
        container.runtimeEnable("disdis");
        Assert.assertTrue(container.isModuleLoaded("disdis"));
    }

    @Test(expected = IllegalStateException.class)
    public void testThatStandardModuleCannotBeEnabledAtRuntime() throws Exception {
        ModuleContainer container = getContainer("uk.co.drnaylor.quickstart.tests.modules.disableable");
        container.loadModules(true);

        Assert.assertFalse(container.isModuleLoaded("disnotdis"));
        container.runtimeEnable("disnotdis");
    }

    @Test
    public void testThatRuntimeDisableableModuleCanBeDisabledAndEnabledAgain() throws Exception {
        ModuleContainer container = getContainer("uk.co.drnaylor.quickstart.tests.modules.disableable");
        container.loadModules(true);

        Assert.assertTrue(container.isModuleLoaded("dis"));
        container.disableModule("dis");
        Assert.assertFalse(container.isModuleLoaded("dis"));
        container.runtimeEnable("dis");
        Assert.assertTrue(container.isModuleLoaded("dis"));
    }
}
