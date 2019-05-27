/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import org.junit.Assert;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.tests.modules.simpleconfig.SimpleConfigModule;
import uk.co.drnaylor.quickstart.tests.modules.simplenoconfig.SimpleNoConfigModule;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

public class ProvidedModuleHolderTests extends FakeLoaderTests {

    @Test
    public void testProvidedModulesLoad() throws Exception {
        ModuleHolder mc = getProvidedContainer(new SimpleConfigModule(), new SimpleNoConfigModule());
        mc.loadModules(true);
        Assert.assertEquals(2, mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).size());
    }

    @Test
    public void testProvidedModulesLoadAndCanBeDisabled() throws Exception {
        ModuleHolder mc = getProvidedContainer(new SimpleConfigModule(), new SimpleNoConfigModule());
        mc.disableModule("simple");
        mc.loadModules(true);
        Assert.assertEquals(1, mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).size());
        Assert.assertTrue(mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).contains("simpleconfig"));
        Assert.assertFalse(mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).contains("simple"));
    }
}
