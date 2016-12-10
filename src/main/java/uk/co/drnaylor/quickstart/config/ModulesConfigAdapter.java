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

/**
 * Configuration adapter that handles the module statuses.
 */
public final class ModulesConfigAdapter extends AbstractConfigAdapter<HashMap<String, LoadingStatus>> {

    public static final String modulesKey = "modules";

    private final TypeToken<HashMap<String, LoadingStatus>> tt = new TypeToken<HashMap<String, LoadingStatus>>() {};
    private final HashMap<String, LoadingStatus> defaults;
    private final LoggerProxy proxy;

    public ModulesConfigAdapter(HashMap<String, LoadingStatus> defaults, LoggerProxy proxy) {
        this.defaults = defaults;
        this.proxy = proxy;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        try {
            return node.setValue(tt, defaults);
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
