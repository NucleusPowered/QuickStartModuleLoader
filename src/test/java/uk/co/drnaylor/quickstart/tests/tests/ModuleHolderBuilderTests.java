/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import org.junit.Assert;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;
import uk.co.drnaylor.quickstart.tests.modules.DisableableModule;
import uk.co.drnaylor.quickstart.tests.modules.TestModule;
import uk.co.drnaylor.quickstart.tests.modules.nontest.FakeModule;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

/**
 * Tests that the {@link ModuleHolder.Builder} can correctly build {@link ModuleHolder}s.
 */
public class ModuleHolderBuilderTests extends FakeLoaderTests {

    /**
     * When there is no package, test we throw a {@link NullPointerException}
     * @throws Exception The exception.
     */
    @Test(expected = NullPointerException.class)
    public void testThatBuilderRequiresPackage() throws Exception {
        // Given this Builder...
        DiscoveryModuleHolder.Builder<TestModule, DisableableModule> builder = DiscoveryModuleHolder.builder(TestModule.class, DisableableModule.class);

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
        DiscoveryModuleHolder.Builder<TestModule, DisableableModule> builder = DiscoveryModuleHolder.builder(TestModule.class, DisableableModule.class);

        // and this package...
        builder.setPackageToScan(FakeModule.packageName());

        // and this enabler.
        builder.setModuleEnabler(BASIC_ENABLER);

        // when we provide no loader and build, then should throw an exception.
        builder.build();
    }

    /**
     * When we have a package and loader, test we get a {@link ModuleHolder} back.
     *
     * @throws QuickStartModuleDiscoveryException Should not happen.
     */
    @Test
    public void testThatBuilderProvidesModuleContainer() throws QuickStartModuleDiscoveryException {
        // Given this Builder
        DiscoveryModuleHolder.Builder<TestModule, DisableableModule> builder = DiscoveryModuleHolder
                .builder(TestModule.class, DisableableModule.class);

        // and this package...
        builder.setPackageToScan(FakeModule.packageName()).setConfigurationLoader(loader);

        // and this enabler.
        builder.setModuleEnabler(BASIC_ENABLER);

        // when we provide no loader and build, then should throw an exception.
        ModuleHolder container = builder.build();
        Assert.assertNotNull(container);
    }


}
