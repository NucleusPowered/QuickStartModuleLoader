/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.scaffolding;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.junit.Before;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FakeLoaderTests {

    protected ConfigurationLoader<ConfigurationNode> loader;
    protected ConfigurationNode n = SimpleConfigurationNode.root();

    @Before
    @SuppressWarnings("unchecked")
    public void beforeTests() throws Exception {
        loader = (ConfigurationLoader<ConfigurationNode>)mock(ConfigurationLoader.class);
        when(loader.createEmptyNode()).thenReturn(SimpleConfigurationNode.root());
        when(loader.load()).thenReturn(n);
    }

    protected ModuleContainer getContainer(String p) throws QuickStartModuleDiscoveryException {
        return ModuleContainer.builder().setConfigurationLoader(loader).setPackageToScan(p).build();
    }
}
