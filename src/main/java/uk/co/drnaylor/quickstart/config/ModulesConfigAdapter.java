/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration adapter that handles the module statues.
 */
public final class ModulesConfigAdapter<T extends ConfigurationLoader<ConfigurationNode>>
        extends AbstractConfigAdapter<ConfigurationNode, T, Map<String, LoadingStatus>> {

    public static final String modulesKey = "modules";

    private final Map<String, LoadingStatus> defaults;

    public ModulesConfigAdapter(Map<String, LoadingStatus> defaults) {
        this.defaults = defaults;
    }

    @Override
    public CommentedConfigurationNode getDefaults() {
        return SimpleCommentedConfigurationNode.root().setValue(defaults);
    }

    @Override
    public Map<String, LoadingStatus> getData(ConfigurationNode node) {
        HashMap<String, LoadingStatus> value = null;
        try {
            value = node.getValue(new TypeToken<HashMap<String, LoadingStatus>>() {});
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        if (value == null) {
            return ImmutableMap.copyOf(defaults);
        }

        return value;
    }

    @Override
    public ConfigurationNode setData(Map<String, LoadingStatus> data) {
        return SimpleConfigurationNode.root().setValue(data);
    }
}
