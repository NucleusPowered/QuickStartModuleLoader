/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import ninja.leaping.configurate.ConfigurationNode;
import org.junit.Assert;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.tests.config.adapters.SimpleWithDefault;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

import static org.junit.Assert.assertEquals;

public class ModuleConfigurationTests extends FakeLoaderTests {

    @Test
    public void testThatMergedDefaultsFromConfigAdapterArePresent() throws Exception {
        // When we load these modules...
        ModuleContainer mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.adapterstest");
        mc.loadModules(true);

        SimpleWithDefault s2 = mc.getConfigAdapterForModule("moduletwo", SimpleWithDefault.class);
        assertEquals(s2.getNode().getNode("test").getString(), "test");
    }

    @Test
    public void testThatUpdatingConfigAdapterPutsValueInCorrectPlace() throws Exception {
        // When we load these modules...
        ModuleContainer mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.adapterstest");
        mc.loadModules(true);

        SimpleWithDefault s2 = mc.getConfigAdapterForModule("moduletwo", SimpleWithDefault.class);
        ConfigurationNode newNode = s2.getNode();
        newNode.getNode("newnode").setValue("result");

        // This should not update the current node yet
        Assert.assertNotEquals("result", n.getNode("moduletwo", "newnode").getString());
        s2.setNode(newNode);

        // Now it should have done.
        Assert.assertEquals("result", n.getNode("moduletwo", "newnode").getString());
    }
}
