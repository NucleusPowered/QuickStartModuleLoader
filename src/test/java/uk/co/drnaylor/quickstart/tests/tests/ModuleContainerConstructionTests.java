/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.config.ModulesConfigAdapter;
import uk.co.drnaylor.quickstart.config.SimpleNodeConfigAdapter;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.tests.config.adapters.SimpleWithDefault;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

import java.util.Set;

public class ModuleContainerConstructionTests extends FakeLoaderTests {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testThatNoModulesThrow() throws Exception {
        expectedException.expect(QuickStartModuleDiscoveryException.class);
        expectedException.expectMessage("No modules were found");

        getContainer("uk.co.drnaylor.quickstart.tests.modules.exceptions.notamodule");
    }

    @Test
    public void testThatDuplicateIDModulesThrow() throws Exception {
        expectedException.expect(QuickStartModuleDiscoveryException.class);
        expectedException.expectMessage("Duplicate module ID \"one\" was discovered - loading cannot continue.");

        getContainer("uk.co.drnaylor.quickstart.tests.modules.exceptions.duplicateid");
    }

    @Test(expected = QuickStartModuleLoaderException.Construction.class)
    public void testThatUnconstructableModulesThrow() throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException.Enabling, QuickStartModuleLoaderException.Construction {
        getContainer("uk.co.drnaylor.quickstart.tests.modules.exceptions.construction").loadModules(true);
    }

    @Test(expected = QuickStartModuleLoaderException.Enabling.class)
    public void testThatUnenableableModulesThrow() throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException.Enabling, QuickStartModuleLoaderException.Construction {
        getContainer("uk.co.drnaylor.quickstart.tests.modules.exceptions.enabling").loadModules(true);
    }

    @Test
    public void testThatSimpleNoConfigModuleConstructsAndEnables() throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException.Enabling, QuickStartModuleLoaderException.Construction {
        getContainer("uk.co.drnaylor.quickstart.tests.modules.simplenoconfig").loadModules(true);
    }

    @Test
    public void testThatSimpleConfigModuleConstructsAndEnables() throws QuickStartModuleDiscoveryException, QuickStartModuleLoaderException.Enabling, QuickStartModuleLoaderException.Construction {
        getContainer("uk.co.drnaylor.quickstart.tests.modules.simpleconfig").loadModules(true);
    }

    @Test
    public void testThatConfigAdaptersGetRegisteredOnConstruction() throws Exception {
        // When we load these modules...
        ModuleContainer mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.adapterstest");
        mc.loadModules(true);

        // ...test that we get three config adapters.
        ModulesConfigAdapter mca = mc.getConfigAdapterForModule(ModulesConfigAdapter.modulesKey, ModulesConfigAdapter.class);
        SimpleNodeConfigAdapter s = mc.getConfigAdapterForModule("moduleone", SimpleNodeConfigAdapter.class);
        SimpleWithDefault s2 = mc.getConfigAdapterForModule("moduletwo", SimpleWithDefault.class);

        assertNotNull(mca);
        assertNotNull(s);
        assertNotNull(s2);
    }

    @Test
    public void testThatModulesAreLoadedAsExpected() throws Exception {
        // When we load these modules...
        ModuleContainer mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.modulestates");
        mc.loadModules(true);

        Set<String> ss = mc.getModules(ModuleContainer.ModuleStatusTristate.ENABLE);
        Assert.assertTrue(ss.contains("man"));
        Assert.assertTrue(ss.contains("fl"));
        Assert.assertFalse(ss.contains("dis"));
    }

    @Test
    public void testThatOneModuleFailingDoesntKillWholeLoader() throws Exception {
        // When we load these modules...
        ModuleContainer mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.exceptions.onefail");
        mc.loadModules(false);

        Set<String> ss = mc.getModules(ModuleContainer.ModuleStatusTristate.ENABLE);
        Assert.assertTrue(ss.contains("prepass"));
        Assert.assertFalse(ss.contains("prefail"));
    }
}
