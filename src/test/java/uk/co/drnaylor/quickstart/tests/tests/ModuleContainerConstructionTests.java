/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.config.ModulesConfigAdapter;
import uk.co.drnaylor.quickstart.config.SimpleNodeConfigAdapter;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.tests.config.adapters.SimpleWithDefault;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ModuleContainerConstructionTests extends FakeLoaderTests {

    private ModuleContainer getContainer(String p) throws QuickStartModuleDiscoveryException {
        return ModuleContainer.builder().setConfigurationLoader(loader).setPackageToScan(p).build();
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
    public void testThatMergedDefaultsFromConfigAdapterArePresent() throws Exception {
        // When we load these modules...
        ModuleContainer mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.adapterstest");
        mc.loadModules(true);

        SimpleWithDefault s2 = mc.getConfigAdapterForModule("moduletwo", SimpleWithDefault.class);
        assertEquals(s2.getNode().getNode("test").getString(), "test");
    }
}
