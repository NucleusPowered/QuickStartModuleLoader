/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.container;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.tests.modules.nontest.FakeModule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that the {@link uk.co.drnaylor.quickstart.ModuleContainer.Builder} can correctly build {@link uk.co.drnaylor.quickstart.ModuleContainer}s.
 */
public class ModuleContainerBuilderTests {

    private ConfigurationLoader<ConfigurationNode> loader;

    @Before
    @SuppressWarnings("unchecked")
    public void beforeTests() throws Exception {
        loader = (ConfigurationLoader<ConfigurationNode>)mock(ConfigurationLoader.class);
        when(loader.createEmptyNode()).thenReturn(SimpleConfigurationNode.root());
        when(loader.load()).thenReturn(SimpleConfigurationNode.root());
    }

    /**
     * When there is no package, test we throw a {@link NullPointerException}
     * @throws Exception The exception.
     */
    @Test(expected = NullPointerException.class)
    public void testThatBuilderRequiresPackage() throws Exception {
        // Given this Builder...
        ModuleContainer.Builder builder = ModuleContainer.builder();

        // When we provide no parameters and build it, then should throw an exception.
        builder.build();
    }

    /**
     * When there is no config loader, test we throw a {@link NullPointerException}
     * @throws Exception The exception.
     */
    @Test(expected = NullPointerException.class)
    public void testThatBuilderRequiresConfigurationLoader() throws Exception {
        // Given this Builder
        ModuleContainer.Builder builder = ModuleContainer.builder();

        // and this package...
        builder.setPackageToScan(FakeModule.packageName());

        // when we provide no loader and build, then should throw an exception.
        builder.build();
    }

    /**
     * When we have a package and loader, test we get a {@link ModuleContainer} back.
     *
     * @throws QuickStartModuleDiscoveryException Should not happen.
     */
    @Test
    public void testThatBuilderProvidesModuleContainer() throws QuickStartModuleDiscoveryException {
        // Given this Builder
        ModuleContainer.Builder builder = ModuleContainer.builder();

        // and this package...
        builder.setPackageToScan(FakeModule.packageName()).setConfigurationLoader(loader);

        // when we provide no loader and build, then should throw an exception.
        ModuleContainer container = builder.build();
        Assert.assertNotNull(container);
    }


}
