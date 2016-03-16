/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class SimpleNodeConfigAdapter<N extends ConfigurationNode> extends AbstractConfigAdapter<N, N> {

    @Override
    protected N generateDefaults(N node) {
        return node;
    }

    @Override
    protected N convertFromConfigurateNode(N node) {
        return node;
    }

    @Override
    protected N insertIntoConfigurateNode(N data) throws ObjectMappingException {
        return data;
    }
}
