/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.config.adapters;

import ninja.leaping.configurate.ConfigurationNode;

public class SimpleWithDefaultAndManualTransform extends SimpleNodeConfigAdapter {

    @Override
    protected void manualTransform(ConfigurationNode node) {
        node.getNode("test").setValue("transformed");
    }

    @Override
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        node.getNode("test").setValue("test");
        node.getNode("test2").setValue("test2");
        return node;
    }
}
