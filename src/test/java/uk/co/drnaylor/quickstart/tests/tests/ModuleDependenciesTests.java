/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import org.junit.Assert;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.exceptions.UndisableableModuleException;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

public class ModuleDependenciesTests extends FakeLoaderTests {

    @Test
    public void testDependentModulesAllLoadWhenEnabled() throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException.Enabling, QuickStartModuleLoaderException.Construction {
        ModuleHolder mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.dependenciestest");
        mc.loadModules(true);
        Assert.assertEquals(3, mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).size());
    }

    @Test
    public void testDisablingModuleThreeOnlyDisabledModuleThree()
            throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException, NoModuleException, UndisableableModuleException {
        ModuleHolder mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.dependenciestest");
        mc.disableModule("modulethree");
        mc.loadModules(true);
        Assert.assertEquals(2, mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).size());
        Assert.assertFalse(mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).contains("modulethree"));
    }


    @Test
    public void testDisablingModuleTwoDisablesModuleThreeToo()
            throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException, NoModuleException, UndisableableModuleException {
        ModuleHolder mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.dependenciestest");
        mc.disableModule("moduletwo");
        mc.loadModules(true);
        Assert.assertEquals(1, mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).size());
        Assert.assertFalse(mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).contains("moduletwo"));
        Assert.assertFalse(mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).contains("modulethree"));
    }

    @Test(expected = QuickStartModuleLoaderException.Construction.class)
    public void testDisablingModuleOneDisablesModuleTwoAndThreeToo()
            throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException, NoModuleException, UndisableableModuleException {
        ModuleHolder mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.dependenciestest");
        mc.disableModule("moduleone");
        mc.loadModules(true);
    }

    @Test(expected = QuickStartModuleDiscoveryException.class)
    public void testCircularDependenciesGetCaught() throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException.Enabling, QuickStartModuleLoaderException.Construction, NoModuleException, UndisableableModuleException {
        ModuleHolder mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.circulardepstest");
        mc.loadModules(true);
    }

    @Test(expected = QuickStartModuleLoaderException.Construction.class)
    public void testMandatoryDependenciesBeingDisabledThrowAnError()
            throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException, NoModuleException, UndisableableModuleException {
        ModuleHolder mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.mandatorydepstest");
        mc.disableModule("moduleone");
        mc.loadModules(true);
    }

    @Test
    public void testDisablingSoftDepDoesNotDisableModule()
            throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException, NoModuleException, UndisableableModuleException {
        ModuleHolder mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.softdepstest");
        mc.disableModule("moduleone");
        mc.loadModules(true);
        Assert.assertEquals(1, mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).size());
        Assert.assertTrue(mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).contains("moduletwo"));
    }

    @Test(expected = QuickStartModuleDiscoveryException.class)
    public void testIncorrectModuleDependencyCausesFailureToLoad() throws QuickStartModuleDiscoveryException {
        getContainer("uk.co.drnaylor.quickstart.tests.modules.missingdeptest");
    }
}
