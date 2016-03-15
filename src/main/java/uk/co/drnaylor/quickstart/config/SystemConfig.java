/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.util.Map;

/**
 * Defines the configuration file that loads the modules, and any {@link AbstractConfigAdapter}s.
 */
public final class SystemConfig<N extends ConfigurationNode, T extends ConfigurationLoader<N>> {

    private Map<String, AbstractConfigAdapter> moduleConfigAdapters = Maps.newHashMap();

    private final T loader;
    private N node;

    public SystemConfig(T loader) throws IOException {
        this.loader = loader;
        load();
    }

    public void load() throws IOException {
        this.node = loader.load();
    }

    public void saveDefaults() {

    }
}
