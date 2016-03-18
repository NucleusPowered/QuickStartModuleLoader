package uk.co.drnaylor.quickstart.tests.config.adapters;

import ninja.leaping.configurate.ConfigurationNode;
import uk.co.drnaylor.quickstart.config.SimpleNodeConfigAdapter;

public class SimpleWithDefault extends SimpleNodeConfigAdapter {

    @Override
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        return node.getNode("test").setValue("test");
    }
}
