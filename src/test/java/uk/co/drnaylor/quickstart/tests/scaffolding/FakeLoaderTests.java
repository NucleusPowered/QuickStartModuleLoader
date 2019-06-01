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
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;
import uk.co.drnaylor.quickstart.holders.ProvidedModuleHolder;
import uk.co.drnaylor.quickstart.loaders.ModuleEnablerBuilder;
import uk.co.drnaylor.quickstart.loaders.PhasedModuleEnabler;
import uk.co.drnaylor.quickstart.tests.modules.DisableableModule;
import uk.co.drnaylor.quickstart.tests.modules.TestModule;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FakeLoaderTests {

    protected final PhasedModuleEnabler<TestModule, DisableableModule> BASIC_ENABLER
            = new ModuleEnablerBuilder<>(TestModule.class, DisableableModule.class).build();

    protected ConfigurationLoader<ConfigurationNode> loader;
    protected ConfigurationNode n = SimpleConfigurationNode.root();
    private final PhasedModuleEnabler<TestModule, DisableableModule> enabler
            = new ModuleEnablerBuilder<>(TestModule.class, DisableableModule.class)
                .createEnablePhase("preenable", (module, moduleHolder) -> module.preEnable())
                .createEnablePhase("enable", (module, moduleHolder) -> module.onEnable())
                .createEnablePhase("postenable", (module, moduleHolder) -> module.postEnable())
                .createDisablePhase("disable", (module, moduleHolder) -> module.onDisable())
                .build();

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

    protected PhasedModuleEnabler<TestModule, DisableableModule> getEnabler() {
        return this.enabler;
    }

    protected ModuleHolder<TestModule, DisableableModule> getContainer(String p) throws QuickStartModuleDiscoveryException {
        ModuleHolder<TestModule, DisableableModule> container = DiscoveryModuleHolder
                .builder(TestModule.class, DisableableModule.class)
                .setModuleEnabler(this.enabler)
                .setConfigurationLoader(loader)
                .setPackageToScan(p)
                .build();
        container.startDiscover();
        return container;
    }

    protected ModuleHolder<TestModule, DisableableModule> getProvidedContainer(TestModule... modules) throws Exception {
        ModuleHolder<TestModule, DisableableModule> container = ProvidedModuleHolder
                .builder(TestModule.class, DisableableModule.class)
                .setModuleEnabler(this.enabler)
                .setConfigurationLoader(loader)
                .setNoMergeIfPresent(true)
                .setModules(Arrays.stream(modules).collect(Collectors.toSet())).build();
        container.startDiscover();
        return container;
    }
}
