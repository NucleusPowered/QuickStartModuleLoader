/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.io.IOException;
import java.util.Map;

/**
 * Defines the configuration file that loads the modules, and any {@link AbstractConfigAdapter}s.
 */
public final class SystemConfig<N extends ConfigurationNode, T extends ConfigurationLoader<N>> {

    private String modulesNode = "modules";

    private Map<String, AbstractConfigAdapter> moduleConfigAdapters = Maps.newHashMap();

    private final T loader;
    private N node;

    SystemConfig(T loader) throws IOException {
        this.loader = loader;
        load();
    }

    public void load() throws IOException {
        this.node = loader.load();
    }

    void attachConfigAdapter(String module, AbstractConfigAdapter configAdapter) {
        if (moduleConfigAdapters.containsKey(module.toLowerCase())) {
            throw new IllegalArgumentException();
        }

        moduleConfigAdapters.put(module.toLowerCase(), configAdapter);
    }

    void saveAdapterDefaults() throws IOException {
        CommentedConfigurationNode n = SimpleCommentedConfigurationNode.root();
        moduleConfigAdapters.forEach((k, v) -> n.getNode(k.toLowerCase()).setValue(v.getDefaults()));

        node.mergeValuesFrom(n);
        save();
    }

    private void save() throws IOException {
        loader.save(node);
    }
}
