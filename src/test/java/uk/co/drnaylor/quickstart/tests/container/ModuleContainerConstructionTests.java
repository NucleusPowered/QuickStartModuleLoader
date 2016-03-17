/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.container;

import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

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
}
