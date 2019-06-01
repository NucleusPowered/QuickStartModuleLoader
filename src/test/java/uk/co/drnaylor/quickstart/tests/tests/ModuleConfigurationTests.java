/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import static org.junit.Assert.assertEquals;

import ninja.leaping.configurate.ConfigurationNode;
import org.junit.Assert;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.tests.config.adapters.SimpleWithDefault;
import uk.co.drnaylor.quickstart.tests.config.adapters.SimpleWithDefaultAndManualTransform;
import uk.co.drnaylor.quickstart.tests.config.adapters.SimpleWithDefaultAndTransform;
import uk.co.drnaylor.quickstart.tests.modules.DisableableModule;
import uk.co.drnaylor.quickstart.tests.modules.TestModule;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

public class ModuleConfigurationTests extends FakeLoaderTests {

    @Test
    public void testThatMergedDefaultsFromConfigAdapterArePresent() throws Exception {
        // When we load these modules...
        ModuleHolder<TestModule, DisableableModule> mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.adapterstest");
        mc.loadModules(true);

        SimpleWithDefault s2 = mc.getConfigAdapterForModule("moduletwo", SimpleWithDefault.class);
        assertEquals(s2.getNode().getNode("test").getString(), "test");
    }

    @Test
    public void testThatUpdatingConfigAdapterPutsValueInCorrectPlace() throws Exception {
        // When we load these modules...
        ModuleHolder<TestModule, DisableableModule> mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.adapterstest");
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

    @Test
    public void testThatUsingATransformationActuallyWorks() throws Exception {
        // When we load these modules...
        ModuleHolder<TestModule, DisableableModule> mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.adapterstransformtest");
        mc.loadModules(true);

        SimpleWithDefaultAndTransform s2 = mc.getConfigAdapterForModule("moduleone", SimpleWithDefaultAndTransform.class);

        Assert.assertTrue(s2.getNode().getNode("test2").isVirtual());
        Assert.assertFalse(s2.getNode().getNode("testmove").isVirtual());
        Assert.assertEquals("test2", s2.getNode().getNode("testmove").getString());
        Assert.assertEquals("test", s2.getNode().getNode("test").getString());
    }

    @Test
    public void testThatUsingAManualTransformationActuallyWorks() throws Exception {
        // When we load these modules...
        ModuleHolder<TestModule, DisableableModule> mc = getContainer("uk.co.drnaylor.quickstart.tests.modules.adaptersmanualtransformtest");
        mc.loadModules(true);

        SimpleWithDefaultAndManualTransform s2 = mc.getConfigAdapterForModule("moduleone", SimpleWithDefaultAndManualTransform.class);

        Assert.assertFalse(s2.getNode().getNode("test2").isVirtual());
        Assert.assertFalse(s2.getNode().getNode("test").isVirtual());
        Assert.assertEquals("test2", s2.getNode().getNode("test2").getString());
        Assert.assertEquals("transformed", s2.getNode().getNode("test").getString());
    }
}
