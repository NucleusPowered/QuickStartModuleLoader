/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.config.adapters;

import ninja.leaping.configurate.ConfigurationNode;

public class SimpleWithDefault extends SimpleNodeConfigAdapter {

    @Override
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        return node.getNode("test").setValue("test");
    }
}
