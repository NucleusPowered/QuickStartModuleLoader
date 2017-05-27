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
import uk.co.drnaylor.quickstart.annotations.DoNotSave;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Configuration adapter that handles the module statuses.
 */
@DoNotSave
public final class ModulesConfigAdapter extends AbstractConfigAdapter<HashMap<String, LoadingStatus>> {

    public static final String modulesKey = "modules";

    private final TypeToken<Map<String, LoadingStatus>> tt = new TypeToken<Map<String, LoadingStatus>>() {};
    private final Map<String, LoadingStatus> defaults;
    private final LoggerProxy proxy;
    private final Map<String, String> descriptions;
    private final String key;
    private final String header;

    public ModulesConfigAdapter(Map<String, LoadingStatus> defaults, Map<String, String> descriptions, LoggerProxy proxy, String modulesKey,
            @Nullable String header) {
        this.defaults = defaults;
        this.descriptions = descriptions;
        this.proxy = proxy;
        this.key = modulesKey;
        this.header = header;
    }

    public final String getModulesKey() {
        return this.key;
    }

    @Override
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        this.defaults.forEach((k, v) -> {
            node.getNode(k).setValue(v.name());
            if (node instanceof CommentedConfigurationNode) {
                String comment = this.descriptions.get(k.toLowerCase());
                if (comment != null && !comment.isEmpty()) {
                    ((CommentedConfigurationNode) node).getNode(k).setComment(comment);
                }
            }
        });

        return node;
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
            String h = this.header == null ? "Available modules to enable or disable. Set each to ENABLED to enable the module, DISABLED"
                    + " to prevent the module from loading or FORCELOAD to load the module even if something else tries to disable it." : this.header;
            ((CommentedConfigurationNode) cn).setComment(h);
        }

        return cn;
    }
}
