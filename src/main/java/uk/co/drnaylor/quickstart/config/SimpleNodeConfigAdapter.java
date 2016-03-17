/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class SimpleNodeConfigAdapter extends AbstractConfigAdapter<ConfigurationNode> {

    @Override
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        return node;
    }

    @Override
    protected ConfigurationNode convertFromConfigurateNode(ConfigurationNode node) {
        return node;
    }

    @Override
    protected ConfigurationNode insertIntoConfigurateNode(ConfigurationNode data) throws ObjectMappingException {
        return data;
    }
}
