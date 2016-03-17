/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import com.google.common.base.Preconditions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import uk.co.drnaylor.quickstart.config.AbstractAdaptableConfig;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.config.ModulesConfigAdapter;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.io.IOException;
import java.util.Map;

/**
 * Defines the configuration file that loads the modules, and any {@link AbstractConfigAdapter}s.
 */
public final class SystemConfig<N extends ConfigurationNode, T extends ConfigurationLoader<N>> extends AbstractAdaptableConfig<N, T> {

    private final String modulesNode = "modules";
    private ModulesConfigAdapter configAdapter;

    SystemConfig(T loader) throws IOException {
        super(loader);
    }

    void attachModulesConfig(Map<String, LoadingStatus> defaults) throws IOException {
        Preconditions.checkNotNull(defaults);
        Preconditions.checkState(configAdapter == null);

        configAdapter = new ModulesConfigAdapter(defaults);
        this.attachConfigAdapter(modulesNode, configAdapter);
    }

    public ModulesConfigAdapter getConfigAdapter() {
        return configAdapter;
    }
}
