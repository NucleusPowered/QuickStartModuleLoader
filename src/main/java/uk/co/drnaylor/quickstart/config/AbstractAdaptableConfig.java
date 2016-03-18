/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A configuration manager that allows for {@link AbstractConfigAdapter} to be attached.
 *
 * @param <N> The type of {@link ConfigurationNode} that this configuration will handle.
 * @param <T> The type of {@link ConfigurationLoader} that this configuration will use.
 */
public class AbstractAdaptableConfig<N extends ConfigurationNode, T extends ConfigurationLoader<N>> {

    private Map<String, AbstractConfigAdapter<?>> moduleConfigAdapters = Maps.newHashMap();

    private final T loader;
    private N node;
    private final Supplier<ConfigurationNode> nodeCreator;

    public AbstractAdaptableConfig(T loader) throws IOException {
        this(loader, loader::createEmptyNode);
    }

    public AbstractAdaptableConfig(T loader, Supplier<ConfigurationNode> nodeCreator) throws IOException {
        Preconditions.checkNotNull(loader);
        Preconditions.checkNotNull(nodeCreator);

        this.loader = loader;
        this.nodeCreator = nodeCreator;
        load();
    }

    /**
     * (Re)loads the configuration file into the base node using the selected loader.
     *
     * @throws IOException if the file could not be loaded.
     */
    public void load() throws IOException {
        this.node = loader.load();
    }

    /**
     * Gets a currently attached {@link AbstractConfigAdapter}
     *
     * @param module The module to get the adapter for
     * @param adapterClass The {@link Class} of the adapter.
     * @param <R> The {@link Class} of the adapter.
     * @return The adapter.
     * @throws NoModuleException If the module has not had a config adapter attached to it.
     * @throws IncorrectAdapterTypeException If the specified {@link Class} is incorrect.
     */
    public final <R extends AbstractConfigAdapter<?>> R getConfigAdapterForModule(String module, Class<R> adapterClass) throws NoModuleException, IncorrectAdapterTypeException {
        if (!moduleConfigAdapters.containsKey(module.toLowerCase())) {
            throw new NoModuleException(module);
        }

        AbstractConfigAdapter<?> aca = moduleConfigAdapters.get(module);
        if (adapterClass.isInstance(aca)) {
            return adapterClass.cast(aca);
        }

        throw new IncorrectAdapterTypeException();
    }

    /**
     * Attaches a {@link AbstractConfigAdapter} to this {@link AbstractAdaptableConfig}.
     *
     * @param module The name of the module that this configuration represents.
     * @param configAdapter The {@link AbstractConfigAdapter} to attach
     * @throws IOException if the configuration defaults could not be saved.
     * @throws IllegalArgumentException if the module has already been attached to.
     * @throws IllegalStateException if the adapter has already been attached.
     */
    @SuppressWarnings("unchecked")
    public final void attachConfigAdapter(String module, AbstractConfigAdapter<?> configAdapter) throws IOException {
        if (moduleConfigAdapters.containsKey(module.toLowerCase())) {
            throw new IllegalArgumentException();
        }

        configAdapter.attachConfig(
                module.toLowerCase(),
                this,
                () -> nodeCreator.get().setValue(node.getNode(module.toLowerCase())),
                n -> node.getNode(module.toLowerCase()).setValue(n),
                nodeCreator);
        moduleConfigAdapters.put(module.toLowerCase(), configAdapter);
    }

    /**
     * Saves the configuration.
     *
     * @throws IOException if the configuration could not be saved.
     */
    public void save() throws IOException {
        loader.save(node);
    }

    public void saveAdapterDefaults() throws IOException {
        CommentedConfigurationNode n = SimpleCommentedConfigurationNode.root();
        moduleConfigAdapters.forEach((k, v) -> {
            // Configurate does something I wasn't expecting. If we set a single value with a key on a node, it seems
            // to be set as the root - which causes havoc! So, we get the parent if it exists, because that's the
            // actual null node we're interested in.
            ConfigurationNode cn = v.getDefaults();
            if (cn.getParent() != null) {
                cn = cn.getParent();
            }

            n.getNode(k.toLowerCase()).setValue(cn);
        });

        node.mergeValuesFrom(n);
        save();
    }
}
