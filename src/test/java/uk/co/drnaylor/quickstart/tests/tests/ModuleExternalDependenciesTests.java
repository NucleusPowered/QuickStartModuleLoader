/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import org.junit.Assert;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

public class ModuleExternalDependenciesTests extends FakeLoaderTests {

    private static final String edtsimple = "uk.co.drnaylor.quickstart.tests.modules.extdependenciestest.simple";
    private static final String edtcomplex = "uk.co.drnaylor.quickstart.tests.modules.extdependenciestest.complex";

    @Test
    public void testModuleWithNoDepsFailsToLoadWhenExternalDepIsNotFound() throws Exception {
        ModuleHolder mc = getContainer(edtsimple);
        mc.loadModules(true);
        Assert.assertTrue(mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).size() == 2);
        Assert.assertTrue(mc.getModules(ModuleHolder.ModuleStatusTristate.DISABLE).size() == 1);
    }

    @Test
    public void testModuleWithDepsFailsToLoadAndRecalculatesWhenExternalDepIsNotFound() throws Exception {
        ModuleHolder mc = getContainer(edtcomplex);
        mc.loadModules(true);
        Assert.assertTrue(mc.getModules(ModuleHolder.ModuleStatusTristate.ENABLE).size() == 1);
        Assert.assertTrue(mc.getModules(ModuleHolder.ModuleStatusTristate.DISABLE).size() == 2);
    }

}
