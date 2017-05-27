/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.LoggerProxy;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration adapter that handles the module statuses.
 */
public final class ModulesConfigAdapter extends AbstractConfigAdapter<HashMap<String, LoadingStatus>> {

    public static final String modulesKey = "modules";

    private final TypeToken<Map<String, LoadingStatus>> tt = new TypeToken<Map<String, LoadingStatus>>() {};
    private final Map<String, LoadingStatus> defaults;
    private final LoggerProxy proxy;
    private final Map<String, String> descriptions;

    public ModulesConfigAdapter(Map<String, LoadingStatus> defaults, Map<String, String> descriptions, LoggerProxy proxy) {
        this.defaults = defaults;
        this.descriptions = descriptions;
        this.proxy = proxy;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        try {
            ConfigurationNode toReturn = node.setValue(tt, defaults);
            if (toReturn instanceof CommentedConfigurationNode) {
                this.descriptions.forEach((k, v) -> {
                    if (v != null && !v.isEmpty()) {
                        ((CommentedConfigurationNode) toReturn).getNode(k).setComment(v);
                    }
                });
            }

            return toReturn;
        } catch (ObjectMappingException e) {
            return node;
        }
    }

    @Override
    protected HashMap<String, LoadingStatus> convertFromConfigurateNode(ConfigurationNode node) {
        HashMap<String, LoadingStatus> value = null;
        try {
            value = node.getValue(new TypeToken<HashMap<String, LoadingStatus>>() {});
        } catch (ObjectMappingException e) {
            proxy.warn(e.getMessage());
        }

        if (value == null) {
            return Maps.newHashMap(defaults);
        }

        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ConfigurationNode insertIntoConfigurateNode(ConfigurationNode cn, HashMap<String, LoadingStatus> data) throws ObjectMappingException {
        cn.setValue(tt, data);
        if (cn instanceof CommentedConfigurationNode) {
            ((CommentedConfigurationNode) cn).setComment("Available modules to enable or disable. Set each to ENABLED to enable the module, DISABLED"
                    + " to prevent the module from loading or FORCELOAD to load the module even if something else tries to disable it.");
        }

        return cn;
    }
}
