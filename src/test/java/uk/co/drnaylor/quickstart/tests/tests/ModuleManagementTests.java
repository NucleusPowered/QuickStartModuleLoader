/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.config.ModulesConfigAdapter;
import uk.co.drnaylor.quickstart.exceptions.UndisableableModuleException;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

public class ModuleManagementTests extends FakeLoaderTests {

    private ModuleContainer mc;

    @Before
    @Override
    public void beforeTests() throws Exception {
        super.beforeTests();

        mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.modulestates");
    }

    @Test
    public void checkEnabledModuleCanBeDisabled() throws Exception {
        mc.disableModule("en");
        mc.loadModules(true);

        checkEnabled("fl", "man");
        checkDisabled("en", "dis");
    }

    @Test
    public void checkDisabledModuleCanPointlesslyBeDisabled() throws Exception {
        mc.disableModule("dis");
        mc.loadModules(true);

        checkEnabled("en", "fl", "man");
        checkDisabled("dis");
    }

    @Test(expected = UndisableableModuleException.class)
    public void checkForceLoadedModuleCannotBeDisabled() throws Exception {
        mc.disableModule("fl");
    }

    @Test(expected = UndisableableModuleException.class)
    public void checkMandatoryModulesCannotBeDisabled() throws Exception {
        mc.disableModule("man");
    }

    @Test
    public void checkForceLoadedByDefaultButActuallyJustEnabledCanActuallyBeDisabled() throws Exception {
        // Force enabled.
        n.getNode(ModulesConfigAdapter.modulesKey, "fl").setValue("enabled");

        // Rebuild.
        mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.modulestates");

        mc.disableModule("fl");
    }

    @Test
    public void checkMandatoryModuleCannotBeDisabledEvenIfSomeoneTriesCreatingAConfigEntryForItThatSaysDisabled() throws Exception {
        // Force enabled.
        n.getNode(ModulesConfigAdapter.modulesKey, "man").setValue("disabled");

        // Rebuild.
        mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.modulestates");

        mc.loadModules(true);

        checkEnabled("en", "fl", "man");
        checkDisabled("dis");
    }

    @Test(expected = UndisableableModuleException.class)
    public void checkMandatoryModuleCannotBeDisabledEvenIfSomeoneTriesCreatingAConfigEntryForItThatSaysEnabledAndThenTriesToDisableItAnyway() throws Exception {
        // Force enabled.
        n.getNode(ModulesConfigAdapter.modulesKey, "man").setValue("enabled");

        // Rebuild.
        mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.modulestates");
        mc.disableModule("man");
    }

    private void checkEnabled(String... ids) throws Exception {
        for (String c : ids) {
            Assert.assertTrue(mc.getModules(ModuleContainer.ModuleStatusTristate.ENABLE).contains(c));
        }
    }

    private void checkDisabled(String... ids) throws Exception {
        for (String c : ids) {
            Assert.assertFalse(mc.getModules(ModuleContainer.ModuleStatusTristate.ENABLE).contains(c));
        }
    }
}
