/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.enums.ConstructionPhase;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;
import uk.co.drnaylor.quickstart.enums.ModulePhase;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.exceptions.UndisableableModuleException;
import uk.co.drnaylor.quickstart.loaders.ModuleEnabler;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
    protected final Map<String, ModuleSpec> discoveredModules = Maps.newLinkedHashMap();

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
     * Provides a way to enable modules.
     */
    private final ModuleEnabler enabler;

    /**
     * Whether the {@link ModuleData} annotation must be present on modules.
     */
    private final boolean requireAnnotation;

    /**
     * Constructs a {@link ModuleContainer} and starts discovery of the modules.
     *
     * @param <N>                 The type of {@link ConfigurationNode} to use.
     * @param configurationLoader The {@link ConfigurationLoader} that contains details of whether the modules should be enabled or not.
     * @param moduleEnabler       The {@link ModuleEnabler} that contains the logic to enable modules.
     * @param loggerProxy         The {@link LoggerProxy} that contains methods to send messages to the logger, or any other source.
     * @param onPreEnable         The {@link Procedure} to run on pre enable, before modules are pre-enabled.
     * @param onEnable            The {@link Procedure} to run on enable, before modules are pre-enabled.
     * @param onPostEnable        The {@link Procedure} to run on post enable, before modules are pre-enabled.
     * @param configOptions       The {@link Function} that converts {@link ConfigurationOptions}.
     * @param requireAnnotation   Whether modules must have the {@link ModuleData} annotation.
     *
     * @throws QuickStartModuleDiscoveryException if there is an error starting the Module Container.
     */
    protected <N extends ConfigurationNode> ModuleContainer(ConfigurationLoader<N> configurationLoader,
                                                            LoggerProxy loggerProxy,
                                                            ModuleEnabler moduleEnabler,
                                                            Procedure onPreEnable,
                                                            Procedure onEnable,
                                                            Procedure onPostEnable,
                                                            Function<ConfigurationOptions, ConfigurationOptions> configOptions,
                                                            boolean requireAnnotation) throws QuickStartModuleDiscoveryException {

        try {
            this.config = new SystemConfig<>(configurationLoader, loggerProxy, configOptions);
            this.loggerProxy = loggerProxy;
            this.enabler = moduleEnabler;
            this.onPreEnable = onPreEnable;
            this.onPostEnable = onPostEnable;
            this.onEnable = onEnable;
            this.requireAnnotation = requireAnnotation;
        } catch (Exception e) {
            throw new QuickStartModuleDiscoveryException("Unable to start QuickStart", e);
        }
    }

    public final void startDiscover() throws QuickStartModuleDiscoveryException {
        try {
            Preconditions.checkState(currentPhase == ConstructionPhase.INITALISED);
            currentPhase = ConstructionPhase.DISCOVERING;

            Set<Class<? extends Module>> modules = discoverModules();
            HashMap<String, ModuleSpec> discovered = Maps.newHashMap();
            modules.forEach(s -> {
                // If we have a module annotation, we are golden.
                if (s.isAnnotationPresent(ModuleData.class)) {
                    ModuleData md = s.getAnnotation(ModuleData.class);
                    discovered.put(md.id().toLowerCase(), new ModuleSpec(s, md));
                } else if (this.requireAnnotation) {
                    loggerProxy.warn(MessageFormat.format("The module class {0} does not have a ModuleData annotation associated with it. "
                            + "It is not being loaded as the module container requires the annotation to be present.", s.getClass().getName()));
                } else {
                    String id = s.getClass().getName().toLowerCase();
                    loggerProxy.warn(MessageFormat.format("The module {0} does not have a ModuleData annotation associated with it. We're just assuming an ID of {0}.", id));
                    discovered.put(id, new ModuleSpec(s, id, id, LoadingStatus.ENABLED, false));
                }
            });

            // Create the dependency map.
            resolveDependencyOrder(discovered);

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

    private void resolveDependencyOrder(Map<String, ModuleSpec> modules) throws Exception {
        // First, get the modules that have no deps.
        processDependencyStep(modules, x -> x.getValue().getDeps().isEmpty() && x.getValue().getSoftDeps().isEmpty());

        while (!modules.isEmpty()) {
            Set<String> addedModules = discoveredModules.keySet();
            processDependencyStep(modules, x -> addedModules.containsAll(x.getValue().getDeps()) && addedModules.containsAll(x.getValue().getSoftDeps()));
        }
    }

    private void processDependencyStep(Map<String, ModuleSpec> modules, Predicate<Map.Entry<String, ModuleSpec>> predicate) throws Exception {
        // Filter on the predicate
        List<Map.Entry<String, ModuleSpec>> modulesToAdd = modules.entrySet().stream().filter(predicate)
                .sorted((x, y) -> x.getValue().isMandatory() == y.getValue().isMandatory() ? x.getKey().compareTo(y.getKey()) : Boolean.compare(x.getValue().isMandatory(), y.getValue().isMandatory()))
                .collect(Collectors.toList());

        if (modulesToAdd.isEmpty()) {
            throw new IllegalStateException("Some modules have circular dependencies: " + modules.keySet().stream().collect(Collectors.joining(", ")));
        }

        modulesToAdd.forEach(x -> {
            discoveredModules.put(x.getKey(), x.getValue());
            modules.remove(x.getKey());
        });

    }

    protected abstract Set<Class<? extends Module>> discoverModules() throws Exception;

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
        Preconditions.checkState(currentPhase != ConstructionPhase.INITALISED && currentPhase != ConstructionPhase.DISCOVERING);
        return discoveredModules.entrySet().stream().filter(enabledOnly.statusPredicate).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    /**
     * Gets an immutable {@link Map} of module IDs to their {@link LoadingStatus} (disabled, enabled, forceload).
     *
     * @return The modules with their loading states.
     */
    public Map<String, LoadingStatus> getModulesWithLoadingState() {
        Preconditions.checkState(currentPhase != ConstructionPhase.INITALISED && currentPhase != ConstructionPhase.DISCOVERING);
        return ImmutableMap.copyOf(discoveredModules.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getStatus())));
    }

    /**
     * Gets whether a module is enabled and loaded.
     *
     * @param moduleId The module ID to check for.
     * @return <code>true</code> if it is enabled.
     * @throws NoModuleException Thrown if the module does not exist and modules have been loaded.
     */
    public boolean isModuleLoaded(String moduleId) throws NoModuleException {
        if (currentPhase != ConstructionPhase.ENABLING && currentPhase != ConstructionPhase.ENABLED) {
            return false;
        }

        ModuleSpec ms = discoveredModules.get(moduleId.toLowerCase());
        if (ms == null) {
            // No module
            throw new NoModuleException(moduleId);
        }

        return ms.getPhase() == ModulePhase.ENABLED;
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
     * @throws QuickStartModuleLoaderException.Construction if the modules cannot be constructed.
     * @throws QuickStartModuleLoaderException.Enabling if the modules cannot be enabled.
     */
    public void loadModules(boolean failOnOneError) throws QuickStartModuleLoaderException.Construction, QuickStartModuleLoaderException.Enabling {
        Preconditions.checkArgument(currentPhase == ConstructionPhase.DISCOVERED);
        currentPhase = ConstructionPhase.ENABLING;

        // Get the modules that are being disabled and mark them as such.
        Set<String> disabledModules = getModules(ModuleStatusTristate.DISABLE);
        while (!disabledModules.isEmpty()) {
            // Find any modules that have dependencies on disabled modules, and disable them.
            List<ModuleSpec> toDisable = getModules(ModuleStatusTristate.ENABLE).stream().map(discoveredModules::get).filter(x -> !Collections.disjoint(disabledModules, x.getDeps())).collect(Collectors.toList());
            if (toDisable.isEmpty()) {
                break;
            }

            if (toDisable.stream().anyMatch(ModuleSpec::isMandatory)) {
                String s = toDisable.stream().filter(ModuleSpec::isMandatory).map(ModuleSpec::getId).collect(Collectors.joining(", "));
                Class<? extends Module> m = toDisable.stream().filter(ModuleSpec::isMandatory).findFirst().get().getModuleClass();
                throw new QuickStartModuleLoaderException.Construction(m, "Tried to disable mandatory module", new IllegalStateException("Dependency failure, tried to disable a mandatory module (" + s + ")"));
            }

            toDisable.forEach(k -> {
                k.setStatus(LoadingStatus.DISABLED);
                disabledModules.add(k.getId());
            });
        }

        // Make sure we get a clean slate here.
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

                // If the module is errored, then we do not continue.
                if (ms.getPhase() == ModulePhase.ERRORED) {
                    continue;
                }

                try {
                    Module m = modules.get(s);
                    v.onModuleAction(enabler, m, ms);
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
     * Refreshes the backing {@link ConfigurationNode} and saves the {@link SystemConfig}.
     *
     * @throws IOException If the config could not be saved.
     */
    public final void refreshSystemConfig() throws IOException {
        config.save(true);
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
        protected boolean requireAnnotation = false;
        protected LoggerProxy loggerProxy;
        protected Procedure onPreEnable = () -> {};
        protected Procedure onEnable = () -> {};
        protected Procedure onPostEnable = () -> {};
        protected Function<ConfigurationOptions, ConfigurationOptions> configurationOptionsTransformer = x -> x;
        protected ModuleEnabler enabler = ModuleEnabler.SIMPLE_INSTANCE;

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
         * Sets a {@link Function} that takes the loader's {@link ConfigurationOptions}, transforms it, and applies it
         * to nodes when they are loaded.
         *
         * <p>
         *     By default, just uses the {@link ConfigurationOptions} of the loader.
         * </p>
         *
         * @param optionsTransformer The transformer
         * @return This {@link Builder} for chaining.
         */
        public T setConfigurationOptionsTransformer(Function<ConfigurationOptions, ConfigurationOptions> optionsTransformer) {
            Preconditions.checkNotNull(optionsTransformer);
            this.configurationOptionsTransformer = optionsTransformer;
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

        /**
         * Sets the {@link ModuleEnabler} to run when enabling modules.
         *
         * @param enabler The {@link ModuleEnabler}, or {@code null} when the default should be used.
         * @return This {@link Builder}, for chaining.
         */
        public T setModuleEnabler(ModuleEnabler enabler) {
            this.enabler = enabler;
            return getThis();
        }

        /**
         * Sets whether {@link Module}s must have a {@link ModuleData} annotation to be considered.
         *
         * @param requireAnnotation <code>true</code> to require, <code>false</code> otherwise.
         * @return The {@link Builder}, for chaining.
         */
        public T setRequireModuleDataAnnotation(boolean requireAnnotation) {
            this.requireAnnotation = requireAnnotation;
            return getThis();
        }

        protected void checkBuild() {
            Preconditions.checkNotNull(configurationLoader);

            if (loggerProxy == null) {
                loggerProxy = DefaultLogger.INSTANCE;
            }

            if (enabler == null) {
                enabler = ModuleEnabler.SIMPLE_INSTANCE;
            }

            Metadata.getStartupMessage().ifPresent(x -> loggerProxy.info(x));
        }

        public abstract R build() throws Exception;
    }

    public enum ModuleStatusTristate {
        ENABLE(k -> k.getValue().getStatus() != LoadingStatus.DISABLED && k.getValue().getPhase() != ModulePhase.ERRORED && k.getValue().getPhase() != ModulePhase.DISABLED),
        DISABLE(k -> !ENABLE.statusPredicate.test(k)),
        ALL(k -> true);

        private final Predicate<Map.Entry<String, ModuleSpec>> statusPredicate;

        ModuleStatusTristate(Predicate<Map.Entry<String, ModuleSpec>> p) {
            statusPredicate = p;
        }
    }

    private interface ConstructPhase {

        void onStart(ModuleContainer container);

        void onModuleAction(ModuleEnabler enabler, Module module, ModuleSpec ms) throws Exception;
    }

    private enum EnablePhase implements ConstructPhase {
        PREENABLE {
            @Override
            public void onStart(ModuleContainer container) {
                container.onPreEnable.invoke();
            }

            @Override
            public void onModuleAction(ModuleEnabler enabler, Module module, ModuleSpec ms) throws Exception {
                enabler.preEnableModule(module);
            }
        },
        ENABLE {
            @Override
            public void onStart(ModuleContainer container) {
                container.onEnable.invoke();
            }

            @Override
            public void onModuleAction(ModuleEnabler enabler, Module module, ModuleSpec ms) throws Exception {
                enabler.enableModule(module);
                ms.setPhase(ModulePhase.ENABLED);
            }
        },
        POSTENABLE {
            @Override
            public void onStart(ModuleContainer container) {
                container.onPostEnable.invoke();
            }

            @Override
            public void onModuleAction(ModuleEnabler enabler, Module module, ModuleSpec ms) throws Exception {
                enabler.postEnableModule(module);
            }
        }
    }
}
