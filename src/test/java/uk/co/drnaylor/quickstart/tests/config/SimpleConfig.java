/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import uk.co.drnaylor.quickstart.config.AbstractAdaptableConfig;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleConfig extends AbstractAdaptableConfig<ConfigurationNode, ConfigurationLoader<ConfigurationNode>> {

    public SimpleConfig(ConfigurationLoader<ConfigurationNode> loader,
                        Supplier<ConfigurationNode> nodeCreator,
                        Function<ConfigurationOptions, ConfigurationOptions> optionsTransformer) throws IOException {
        super(loader, nodeCreator, optionsTransformer);
    }
}
