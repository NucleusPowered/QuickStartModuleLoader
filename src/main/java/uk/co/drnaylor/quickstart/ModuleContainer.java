/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.enums.ConstructionPhase;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;
import uk.co.drnaylor.quickstart.enums.ModulePhase;
import uk.co.drnaylor.quickstart.exceptions.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The ModuleContainer contains all modules for a particular modular system. It scans the provided {@link ClassLoader}
 * classpath for {@link Module}s which has a root in the provided package. It handles all the discovery, module config
 * file generation, loading and enabling of modules.
 *
 * <p>
 *     A system may have multiple module containers. Each module container is completely separate from one another.
 * </p>
 */
public abstract class ModuleContainer {

    /**
     * The current phase of the container.
     */
    private ConstructionPhase currentPhase = ConstructionPhase.INITALISED;

    /**
     * The modules that have been discovered by the container.
     */
    protected final Map<String, ModuleSpec> discoveredModules = Maps.newHashMap();

    /**
     * Contains the main configuration file.
     */
    protected final SystemConfig<?, ? extends ConfigurationLoader<?>> config;

    /**
     * The logger to use.
     */
    protected final LoggerProxy loggerProxy;

    /**
     * Fires when the PREENABLE phase starts.
     */
    private final Procedure onPreEnable;

    /**
     * Fires when the ENABLE phase starts.
     */
    private final Procedure onEnable;

    /**
     * Fires when the POSTENABLE phase starts.
     */
    private final Procedure onPostEnable;

    /**
     * Constructs a {@link ModuleContainer} and starts discovery of the modules.
     *
     * @param configurationLoader The {@link ConfigurationLoader} that contains details of whether the modules should be enabled or not.
     *
     * @throws QuickStartModuleDiscoveryException if there is an error starting the Module Container.
     */
    protected <N extends ConfigurationNode> ModuleContainer(ConfigurationLoader<N> configurationLoader,
                                                          LoggerProxy loggerProxy,
                                                          Procedure onPreEnable, Procedure onEnable, Procedure onPostEnable) throws QuickStartModuleDiscoveryException {

        try {
            this.config = new SystemConfig<>(configurationLoader, loggerProxy);
            this.loggerProxy = loggerProxy;
            this.onPreEnable = onPreEnable;
            this.onPostEnable = onPostEnable;
            this.onEnable = onEnable;
        } catch (Exception e) {
            throw new QuickStartModuleDiscoveryException("Unable to start QuickStart", e);
        }
    }

    public final void startDiscover() throws QuickStartModuleDiscoveryException {
        try {
            Preconditions.checkState(currentPhase == ConstructionPhase.INITALISED);
            currentPhase = ConstructionPhase.DISCOVERING;

            discoverModules();

            // Modules discovered. Create the Module Config adapter.
            Map<String, LoadingStatus> m = discoveredModules.entrySet().stream().filter(x -> !x.getValue().isMandatory())
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getStatus()));

            // Attaches config adapter and loads in the defaults.
            config.attachModulesConfig(m);
            config.saveAdapterDefaults();

            // Load what we have in config into our discovered modules.
            try {
                config.getConfigAdapter().getNode().forEach((k, v) -> {
                    try {
                        ModuleSpec ms = discoveredModules.get(k);
                        if (ms != null) {
                            ms.setStatus(v);
                        } else {
                            loggerProxy.warn(String.format("Ignoring module entry %s in the configuration file: module does not exist.", k));
                        }
                    } catch (IllegalStateException ex) {
                        loggerProxy.warn("A mandatory module can't have its status changed by config. Falling back to FORCELOAD for " + k);
                    }
                });
            } catch (ObjectMappingException e) {
                loggerProxy.warn("Could not load modules config, falling back to defaults.");
                e.printStackTrace();
            }

            // Modules have been discovered.
            currentPhase = ConstructionPhase.DISCOVERED;
        } catch (Exception e) {
            throw new QuickStartModuleDiscoveryException("Unable to discover QuickStart modules", e);
        }
    }

    protected abstract void discoverModules() throws Exception;

    /**
     * Gets the current phase of the module loader.
     *
     * @return The {@link ConstructionPhase}
     */
    public ConstructionPhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Gets a set of IDs of modules that are going to be loaded.
     *
     * @return The modules that are going to be loaded.
     */
    public Set<String> getModules() {
        return getModules(ModuleStatusTristate.ENABLE);
    }

    /**
     * Gets a set of IDs of modules.
     *
     * @param enabledOnly If <code>true</code>, only return modules that are going to be loaded.
     * @return The modules.
     */
    public Set<String> getModules(final ModuleStatusTristate enabledOnly) {
        Preconditions.checkNotNull(enabledOnly);
        Preconditions.checkArgument(currentPhase != ConstructionPhase.INITALISED && currentPhase != ConstructionPhase.DISCOVERING);
        return discoveredModules.entrySet().stream().filter(enabledOnly.statusPredicate).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    /**
     * Requests that a module be disabled. This can only be run during the {@link ConstructionPhase#DISCOVERED} phase.
     *
     * @param moduleName The ID of the module.
     * @throws UndisableableModuleException if the module can't be disabled.
     * @throws NoModuleException if the module does not exist.
     */
    public void disableModule(String moduleName) throws UndisableableModuleException, NoModuleException {
        Preconditions.checkArgument(currentPhase == ConstructionPhase.DISCOVERED);

        ModuleSpec ms = discoveredModules.get(moduleName.toLowerCase());
        if (ms == null) {
            // No module
            throw new NoModuleException(moduleName);
        }

        if (ms.isMandatory() || ms.getStatus() == LoadingStatus.FORCELOAD) {
            throw new UndisableableModuleException(moduleName);
        }

        ms.setStatus(LoadingStatus.DISABLED);
    }

    protected abstract Module getModule(ModuleSpec spec) throws Exception;

    /**
     * Starts the module construction and enabling phase. This is the final phase for loading the modules.
     *
     * <p>
     *     Once this method is called, modules can no longer be removed.
     * </p>
     *
     * @param failOnOneError If set to <code>true</code>, one module failure will mark the whole loading sequence as failed.
     *                       Otherwise, no modules being constructed will cause a failure.
     *
     * @throws uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException.Construction if the modules cannot be constructed.
     * @throws uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException.Enabling if the modules cannot be enabled.
     */
    public void loadModules(boolean failOnOneError) throws QuickStartModuleLoaderException.Construction, QuickStartModuleLoaderException.Enabling {
        Preconditions.checkArgument(currentPhase == ConstructionPhase.DISCOVERED);
        currentPhase = ConstructionPhase.ENABLING;

        // Get the modules that are being disabled and mark them as such.
        getModules(ModuleStatusTristate.DISABLE).forEach(k -> discoveredModules.get(k).setPhase(ModulePhase.DISABLED));

        // Modules to enable.
        Map<String, Module> modules = Maps.newConcurrentMap();

        // Construct them
        for (String s : getModules(ModuleStatusTristate.ENABLE)) {
            ModuleSpec ms = discoveredModules.get(s);
            try {
                modules.put(s, getModule(ms));
                ms.setPhase(ModulePhase.CONSTRUCTED);
            } catch (Exception construction) {
                construction.printStackTrace();
                ms.setPhase(ModulePhase.ERRORED);
                loggerProxy.error("The module " + ms.getModuleClass().getName() + " failed to construct.");

                if (failOnOneError) {
                    currentPhase = ConstructionPhase.ERRORED;
                    throw new QuickStartModuleLoaderException.Construction(ms.getModuleClass(), "The module " + ms.getModuleClass().getName() + " failed to construct.", construction);
                }
            }
        }

        if (modules.isEmpty()) {
            currentPhase = ConstructionPhase.ERRORED;
            throw new QuickStartModuleLoaderException.Construction(null, "No modules were constructed.", null);
        }

        // Enter Config Adapter phase - attaching before enabling so that enable methods can get any associated configurations.
        for (String s : modules.keySet()) {
            Module m = modules.get(s);
            Optional<AbstractConfigAdapter<?>> a = m.getConfigAdapter();
            if (a.isPresent()) {
                try {
                    config.attachConfigAdapter(s, a.get());
                } catch (IOException e) {
                    e.printStackTrace();
                    if (failOnOneError) {
                        throw new QuickStartModuleLoaderException.Enabling(m.getClass(), "Failed to attach config.", e);
                    }
                }
            }
        }

        // Enter Enable phase.
        Map<String, Module> c = new HashMap<>(modules);

        for (EnablePhase v : EnablePhase.values()) {
            loggerProxy.info(String.format("Starting phase: %s", v.name()));
            v.onStart(this);
            for (String s : c.keySet()) {
                ModuleSpec ms = discoveredModules.get(s);

                try {
                    Module m = modules.get(s);
                    v.onModuleAction(m, ms);
                } catch (Exception construction) {
                    construction.printStackTrace();
                    modules.remove(s);

                    if (v != EnablePhase.POSTENABLE) {
                        ms.setPhase(ModulePhase.ERRORED);
                        loggerProxy.error("The module " + ms.getModuleClass().getName() + " failed to enable.");

                        if (failOnOneError) {
                            currentPhase = ConstructionPhase.ERRORED;
                            throw new QuickStartModuleLoaderException.Enabling(ms.getModuleClass(), "The module " + ms.getModuleClass().getName() + " failed to enable.", construction);
                        }
                    } else {
                        loggerProxy.error("The module " + ms.getModuleClass().getName() + " failed to post-enable.");
                    }
                }
            }
        }

        if (c.isEmpty()) {
            currentPhase = ConstructionPhase.ERRORED;
            throw new QuickStartModuleLoaderException.Enabling(null, "No modules were enabled.", null);
        }

        try {
            config.saveAdapterDefaults();
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentPhase = ConstructionPhase.ENABLED;
    }

    @SuppressWarnings("unchecked")
    public final <R extends AbstractConfigAdapter<?>> R getConfigAdapterForModule(String module, Class<R> adapterClass) throws NoModuleException, IncorrectAdapterTypeException {
        return config.getConfigAdapterForModule(module, adapterClass);
    }

    /**
     * Saves the {@link SystemConfig}.
     *
     * @throws IOException If the config could not be saved.
     */
    public final void saveSystemConfig() throws IOException {
        config.save();
    }

    /**
     * Reloads the {@link SystemConfig}, but does not change any module status.
     *
     * @throws IOException If the config could not be reloaded.
     */
    public final void reloadSystemConfig() throws IOException {
        config.load();
    }

    /**
     * Builder class to create a {@link ModuleContainer}
     */
    protected static abstract class Builder<R extends ModuleContainer, T extends Builder<R, T>> {

        protected ConfigurationLoader<? extends ConfigurationNode> configurationLoader;
        protected LoggerProxy loggerProxy;
        protected Procedure onPreEnable = () -> {};
        protected Procedure onEnable = () -> {};
        protected Procedure onPostEnable = () -> {};

        protected abstract T getThis();

        /**
         * Sets the {@link ConfigurationLoader} that will handle the module loading.
         *
         * @param configurationLoader The loader to use.
         * @return This {@link Builder}, for chaining.
         */
        public T setConfigurationLoader(ConfigurationLoader<? extends ConfigurationNode> configurationLoader) {
            this.configurationLoader = configurationLoader;
            return getThis();
        }

        /**
         * Sets the {@link LoggerProxy} to use for log messages.
         *
         * @param loggerProxy The logger proxy to use.
         * @return This {@link Builder}, for chaining.
         */
        public T setLoggerProxy(LoggerProxy loggerProxy) {
            this.loggerProxy = loggerProxy;
            return getThis();
        }

        /**
         * Sets the {@link Procedure} to run when the pre-enable phase is about to start.
         *
         * @param onPreEnable The {@link Procedure}
         * @return This {@link Builder}, for chaining.
         */
        public T setOnPreEnable(Procedure onPreEnable) {
            Preconditions.checkNotNull(onPreEnable);
            this.onPreEnable = onPreEnable;
            return getThis();
        }

        /**
         * Sets the {@link Procedure} to run when the enable phase is about to start.
         *
         * @param onEnable The {@link Procedure}
         * @return This {@link Builder}, for chaining.
         */
        public T setOnEnable(Procedure onEnable) {
            Preconditions.checkNotNull(onEnable);
            this.onEnable = onEnable;
            return getThis();
        }

        /**
         * Sets the {@link Procedure} to run when the post-enable phase is about to start.
         *
         * @param onPostEnable The {@link Procedure}
         * @return This {@link Builder}, for chaining.
         */
        public T setOnPostEnable(Procedure onPostEnable) {
            Preconditions.checkNotNull(onPostEnable);
            this.onPostEnable = onPostEnable;
            return getThis();
        }

        protected void checkBuild() {
            Preconditions.checkNotNull(configurationLoader);

            if (loggerProxy == null) {
                loggerProxy = DefaultLogger.INSTANCE;
            }

            Metadata.getStartupMessage().ifPresent(x -> loggerProxy.info(x));
        }

        public abstract R build() throws Exception;
    }

    public enum ModuleStatusTristate {
        ENABLE(k -> k.getValue().getStatus() != LoadingStatus.DISABLED),
        DISABLE(k -> k.getValue().getStatus() == LoadingStatus.DISABLED),
        ALL(k -> true);

        private final Predicate<Map.Entry<String, ModuleSpec>> statusPredicate;

        ModuleStatusTristate(Predicate<Map.Entry<String, ModuleSpec>> p) {
            statusPredicate = p;
        }
    }

    private interface ConstructPhase {

        void onStart(ModuleContainer container);

        void onModuleAction(Module module, ModuleSpec ms) throws Exception;
    }

    private enum EnablePhase implements ConstructPhase {
        PREENABLE {
            @Override
            public void onStart(ModuleContainer container) {
                container.onPreEnable.invoke();
            }

            @Override
            public void onModuleAction(Module module, ModuleSpec ms) throws Exception {
                module.preEnable();
            }
        },
        ENABLE {
            @Override
            public void onStart(ModuleContainer container) {
                container.onEnable.invoke();
            }

            @Override
            public void onModuleAction(Module module, ModuleSpec ms) throws Exception {
                module.onEnable();
                ms.setPhase(ModulePhase.ENABLED);
            }
        },
        POSTENABLE {
            @Override
            public void onStart(ModuleContainer container) {
                container.onPostEnable.invoke();
            }

            @Override
            public void onModuleAction(Module module, ModuleSpec ms) throws Exception {
                module.postEnable();
            }
        }
    }
}
