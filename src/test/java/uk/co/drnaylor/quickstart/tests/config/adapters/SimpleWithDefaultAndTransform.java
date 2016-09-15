/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.config.adapters;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import uk.co.drnaylor.quickstart.config.SimpleNodeConfigAdapter;

import java.util.List;

public class SimpleWithDefaultAndTransform extends SimpleNodeConfigAdapter {

    @Override
    protected List<Transformation> getTransformations() {
        return Lists.newArrayList(
            new Transformation(new Object[] {"test2"}, ((inputPath, valueAtPath) -> new Object[] {"testmove" }))
        );
    }

    @Override
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        node.getNode("test").setValue("test");
        node.getNode("test2").setValue("test2");
        return node;
    }
}
