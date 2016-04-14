/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.constructors.ModuleConstructor;
import uk.co.drnaylor.quickstart.constructors.SimpleModuleConstructor;
import uk.co.drnaylor.quickstart.enums.ConstructionPhase;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;
import uk.co.drnaylor.quickstart.enums.ModulePhase;
import uk.co.drnaylor.quickstart.exceptions.*;

import java.io.IOException;
import java.text.MessageFormat;
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
public final class ModuleContainer {

    /**
     * Gets a builder to create a {@link ModuleContainer}
     *
     * @return A {@link ModuleContainer.Builder} for building a {@link ModuleContainer}
     */
    public static ModuleContainer.Builder builder() {
        return new Builder();
    }

    /**
     * The root of the package to scan.
     */
    private final String packageLocation;

    /**
     * The {@link ClassLoader} to scan for new modules.
     */
    private final ClassLoader classLoader;

    /**
     * The current phase of the container.
     */
    private ConstructionPhase currentPhase = ConstructionPhase.INITALISED;

    /**
     * The modules that have been discovered by the container.
     */
    private final Map<String, ModuleSpec> discoveredModules = Maps.newHashMap();

    /**
     * Contains the main configuration file.
     */
    private final SystemConfig<?, ? extends ConfigurationLoader<?>> config;

    /**
     * Provides the methods to use to construct the module.
     */
    private final ModuleConstructor constructor;

    /**
     * The logger to use.
     */
    private final LoggerProxy loggerProxy;

    /**
     * The classes that were loaded by the module loader.
     */
    private final Set<Class<?>> loadedClasses = Sets.newHashSet();

    /**
     * Constructs a {@link ModuleContainer} and starts discovery of the modules.
     *
     * @param configurationLoader The {@link ConfigurationLoader} that contains details of whether the modules should be enabled or not.
     * @param loader The {@link ClassLoader} that contains the classpath in which the modules are located.
     * @param packageBase The root name of the package to scan for modules.
     *
     * @throws QuickStartModuleDiscoveryException if there is an error starting the Module Container.
     */
    private <N extends ConfigurationNode> ModuleContainer(ConfigurationLoader<N> configurationLoader, ClassLoader loader, String packageBase, ModuleConstructor constructor, LoggerProxy loggerProxy) throws QuickStartModuleDiscoveryException {

        try {
            this.config = new SystemConfig<>(configurationLoader, loggerProxy);
            this.constructor = constructor;
            this.classLoader = loader;
            this.packageLocation = packageBase;
            this.loggerProxy = loggerProxy;

            discoverModules();
        } catch (Exception e) {
            throw new QuickStartModuleDiscoveryException("Unable to start QuickStart", e);
        }
    }

    /**
     * Starts discovery of modules.
     */
    private void discoverModules() throws IOException, QuickStartModuleDiscoveryException {
        Preconditions.checkState(currentPhase == ConstructionPhase.INITALISED);
        currentPhase = ConstructionPhase.DISCOVERING;

        // Get the modules out.
        Set<ClassPath.ClassInfo> ci = ClassPath.from(classLoader).getTopLevelClassesRecursive(packageLocation);
        loadedClasses.addAll(ci.stream().map(ClassPath.ClassInfo::load).collect(Collectors.toSet()));
        Set<Class<? extends Module>> modules = loadedClasses.stream().filter(Module.class::isAssignableFrom)
                .map(x -> (Class<? extends Module>)x.asSubclass(Module.class)).collect(Collectors.toSet());

        if (modules.isEmpty()) {
            throw new QuickStartModuleDiscoveryException("No modules were found", null);
        }

        // Put the modules into the discoverer.
        modules.forEach(s -> {
            // If we have a module annotation, we are golden.
            if (s.isAnnotationPresent(ModuleData.class)) {
                ModuleData md = s.getAnnotation(ModuleData.class);
                discoveredModules.put(md.id().toLowerCase(), new ModuleSpec(s, md));
            } else {
                String id = s.getName().toLowerCase();
                loggerProxy.warn(MessageFormat.format("The module {0} does not have a ModuleData annotation associated with it. We're just assuming an ID of {0}.", id));
                discoveredModules.put(id, new ModuleSpec(s, id, LoadingStatus.ENABLED, false));
            }
        });

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
    }

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
                modules.put(s, constructor.constructModule(ms.getModuleClass()));
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
            for (String s : c.keySet()) {
                ModuleSpec ms = discoveredModules.get(s);

                try {
                    Module m = modules.get(s);
                    v.construct(constructor, m, ms);
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
     * Reloads the {@link SystemConfig}, but does not change any module status.
     *
     * @throws IOException If the config could not be reloaded.
     */
    public final void reloadSystemConfig() throws IOException {
        config.load();
    }

    /**
     * Gets the {@link Class}es that were scanned during the module discovery phase.
     *
     * @return Gets a {@link Set} of the loaded classes.
     */
    public final Set<Class<?>> getLoadedClasses() {
        return ImmutableSet.copyOf(this.loadedClasses);
    }

    /**
     * Builder class to create a {@link ModuleContainer}
     */
    public static class Builder {

        private ConfigurationLoader<? extends ConfigurationNode> configurationLoader;
        private String packageToScan;
        private ModuleConstructor constructor;
        private ClassLoader classLoader;
        private LoggerProxy loggerProxy;

        /**
         * Sets the {@link ConfigurationLoader} that will handle the module loading.
         *
         * @param configurationLoader The loader to use.
         * @return This {@link Builder}, for chaining.
         */
        public Builder setConfigurationLoader(ConfigurationLoader<? extends ConfigurationNode> configurationLoader) {
            this.configurationLoader = configurationLoader;
            return this;
        }

        /**
         * Sets the root package name to scan.
         *
         * @param packageToScan The root of the package (for example, <code>uk.co.drnaylor.quickstart</code> will scan
         *                      that package and all subpackages, such as <code>uk.co.drnaylor.quickstart.config</code>)
         * @return This {@link Builder}, for chaining.
         */
        public Builder setPackageToScan(String packageToScan) {
            this.packageToScan = packageToScan;
            return this;
        }

        /**
         * Sets the {@link ModuleConstructor} to use when building the module objects.
         *
         * @param constructor The constructor to use
         * @return This {@link Builder}, for chaining.
         */
        public Builder setConstructor(ModuleConstructor constructor) {
            this.constructor = constructor;
            return this;
        }

        /**
         * Sets the {@link ClassLoader} to use when scanning the classpath.
         *
         * @param classLoader The class loader to use.
         * @return This {@link Builder}, for chaining.
         */
        public Builder setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Sets the {@link LoggerProxy} to use for log messages.
         *
         * @param loggerProxy The logger proxy to use.
         * @return This {@link Builder}, for chaining.
         */
        public Builder setLoggerProxy(LoggerProxy loggerProxy) {
            this.loggerProxy = loggerProxy;
            return this;
        }

        /**
         * Builds a {@link ModuleContainer}.
         *
         * @return The {@link ModuleContainer}.
         * @throws QuickStartModuleDiscoveryException if the configuration loader cannot load data from the file.
         */
        public ModuleContainer build() throws QuickStartModuleDiscoveryException {
            Preconditions.checkNotNull(packageToScan);
            Preconditions.checkNotNull(configurationLoader);

            if (constructor == null) {
                constructor = SimpleModuleConstructor.INSTANCE;
            }

            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            if (loggerProxy == null) {
                loggerProxy = DefaultLogger.INSTANCE;
            }

            Metadata.getStartupMessage().ifPresent(x -> loggerProxy.info(x));
            return new ModuleContainer(configurationLoader, classLoader, packageToScan, constructor, loggerProxy);
        }
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

        void construct(ModuleConstructor constructor, Module module, ModuleSpec ms) throws Exception;
    }

    private enum EnablePhase implements ConstructPhase {
        PREENABLE {
            @Override
            public void construct(ModuleConstructor constructor, Module module, ModuleSpec ms) throws Exception {
                constructor.preEnableModule(module);
            }
        },
        ENABLE {
            @Override
            public void construct(ModuleConstructor constructor, Module module, ModuleSpec ms) throws Exception {
                constructor.enableModule(module);
                ms.setPhase(ModulePhase.ENABLED);
            }
        },
        POSTENABLE {
            @Override
            public void construct(ModuleConstructor constructor, Module module, ModuleSpec ms) throws Exception {
                constructor.postEnableModule(module);
            }
        }
    }
}
