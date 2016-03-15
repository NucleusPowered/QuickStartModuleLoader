/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public abstract class SimpleNodeConfigAdapter<N extends ConfigurationNode, T extends ConfigurationLoader<N>> extends AbstractConfigAdapter<N, T, N> {

    @Override
    public N getData(N data) {
        return data;
    }

    @Override
    public N setData(N data) {
        return data;
    }
}
