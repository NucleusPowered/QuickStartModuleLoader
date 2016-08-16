/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import org.junit.Assert;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.modulecontainers.DiscoveryModuleContainer;
import uk.co.drnaylor.quickstart.tests.modules.nontest.FakeModule;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

/**
 * Tests that the {@link uk.co.drnaylor.quickstart.ModuleContainer.Builder} can correctly build {@link uk.co.drnaylor.quickstart.ModuleContainer}s.
 */
public class ModuleContainerBuilderTests extends FakeLoaderTests {

    /**
     * When there is no package, test we throw a {@link NullPointerException}
     * @throws Exception The exception.
     */
    @Test(expected = NullPointerException.class)
    public void testThatBuilderRequiresPackage() throws Exception {
        // Given this Builder...
        DiscoveryModuleContainer.Builder builder = DiscoveryModuleContainer.builder();

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
        DiscoveryModuleContainer.Builder builder = DiscoveryModuleContainer.builder();

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
        DiscoveryModuleContainer.Builder builder = DiscoveryModuleContainer.builder();

        // and this package...
        builder.setPackageToScan(FakeModule.packageName()).setConfigurationLoader(loader);

        // when we provide no loader and build, then should throw an exception.
        ModuleContainer container = builder.build();
        Assert.assertNotNull(container);
    }


}
