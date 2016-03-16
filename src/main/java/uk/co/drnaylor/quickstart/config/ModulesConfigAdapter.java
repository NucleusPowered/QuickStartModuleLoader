/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration adapter that handles the module statuses.
 */
public final class ModulesConfigAdapter<N extends ConfigurationNode> extends AbstractConfigAdapter<N, Map<String, LoadingStatus>> {

    public static final String modulesKey = "modules";

    private final Map<String, LoadingStatus> defaults;

    public ModulesConfigAdapter(Map<String, LoadingStatus> defaults) {
        this.defaults = defaults;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected N generateDefaults(N node) {
        return (N)node.setValue(defaults);
    }

    @Override
    protected Map<String, LoadingStatus> convertFromConfigurateNode(N node) {
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
    @SuppressWarnings("unchecked")
    protected N insertIntoConfigurateNode(Map<String, LoadingStatus> data) {
        return (N)this.getNewNode().setValue(data);
    }
}
