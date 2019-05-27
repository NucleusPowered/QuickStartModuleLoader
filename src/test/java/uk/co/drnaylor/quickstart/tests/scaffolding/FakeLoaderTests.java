/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.scaffolding;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.junit.Before;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;
import uk.co.drnaylor.quickstart.holders.ProvidedModuleHolder;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FakeLoaderTests {

    protected ConfigurationLoader<ConfigurationNode> loader;
    protected ConfigurationNode n = SimpleConfigurationNode.root();

    @Before
    @SuppressWarnings("unchecked")
    public void beforeTests() throws Exception {
        loader = (ConfigurationLoader<ConfigurationNode>)mock(ConfigurationLoader.class);
        when(loader.createEmptyNode()).thenReturn(SimpleConfigurationNode.root());
        when(loader.createEmptyNode(any(ConfigurationOptions.class))).thenReturn(SimpleConfigurationNode.root());
        when(loader.load()).thenReturn(n);
        when(loader.load(any(ConfigurationOptions.class))).thenReturn(n);
        when(loader.getDefaultOptions()).thenReturn(ConfigurationOptions.defaults());
    }

    protected ModuleHolder<Module> getContainer(String p) throws QuickStartModuleDiscoveryException {
        ModuleHolder<Module> container = DiscoveryModuleHolder
                .builder(Module.class)
                .setConfigurationLoader(loader)
                .setPackageToScan(p)
                .build();
        container.startDiscover();
        return container;
    }

    protected ModuleHolder<Module> getProvidedContainer(Module... modules) throws Exception {
        ModuleHolder<Module> container = ProvidedModuleHolder.builder(Module.class).setConfigurationLoader(loader)
                .setNoMergeIfPresent(true).setModules(Arrays.stream(modules).collect(Collectors.toSet())).build();
        container.startDiscover();
        return container;
    }
}
