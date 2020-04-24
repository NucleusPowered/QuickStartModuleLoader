/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.config.NoMergeIfPresent;
import uk.co.drnaylor.quickstart.config.TypedAbstractConfigAdapter;
import uk.co.drnaylor.quickstart.enums.ConstructionPhase;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;
import uk.co.drnaylor.quickstart.enums.ModulePhase;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.MissingDependencyException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.exceptions.UndisableableModuleException;
import uk.co.drnaylor.quickstart.loaders.PhasedModuleEnabler;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * The {@link ModuleHolder} contains all modules for a particular modular system.
 * It handles all the discovery, module config file generation, loading and enabling of modules.
 *
 * <p>
 *     A system may have multiple module holders. Each module holders is completely separate from one another.
 * </p>
 */
public abstract class ModuleHolder<M extends Module, D extends M> {

    /**
     * The class type {@link M} of the basic class.
     */
    private final Class<M> baseClass;

    /**
     * The class type {@link D} of the module type.
     */
    private final Class<D> disableableClass;

    /**
     * Whether the module are disableable at runtime.
     */
    private final boolean allowDisabling;

    /**
     * The current phase of the container.
     */
    private ConstructionPhase currentPhase = ConstructionPhase.INITALISED;

    /**
     * The modules that have been discovered by the container.
     */
    private final Map<String, ModuleMetadata<? extends M>> discoveredModules = Maps.newLinkedHashMap();

    /**
     * Loaded modules that can be disabled.
     */
    private final Map<String, ModuleMetadata<? extends D>> enabledDisableableModules = Maps.newHashMap();

    /**
     * The modules that are enabled.
     */
    private final Map<String, M> enabledModules = Maps.newHashMap();

    /**
     * The actual disableable module objects
     */
    private final Map<String, D> disableableModules = Maps.newHashMap();

    /**
     * Contains the main configuration file.
     */
    protected final SystemConfig<?, M> config;

    /**
     * The logger to use.
     */
    protected final LoggerProxy loggerProxy;

    /**
     * Provides a way to enable modules.
     */
    private final PhasedModuleEnabler<M, D> enabler;

    /**
     * Whether the {@link ModuleData} annotation must be present on modules.
     */
    private final boolean requireAnnotation;

    /**
     * Whether or not to take note of {@link NoMergeIfPresent} annotations on configs.
     */
    private final boolean processDoNotMerge;

    /**
     * The function that determines configuration headers for an entry.
     */
    private final Function<M, String> headerProcessor;

    /**
     * The function that determines the descriptions for a module's name.
     */
    private final Function<Class<? extends M>, String> descriptionProcessor;

    /**
     * The name of the configuration section that contains the module flags
     */
    private final String moduleSection;

    /**
     * The header of the configuration section that contains the module flags
     */
    @Nullable private final String moduleSectionHeader;

    protected <R extends ModuleHolder<M, D>, B extends Builder<M, D, R, B>> ModuleHolder(B builder)
            throws QuickStartModuleDiscoveryException {
        try {
            this.baseClass = builder.moduleType;
            this.disableableClass = builder.disableableClass;
            this.config = new SystemConfig<>(builder.configurationLoader, builder.loggerProxy, builder.configurationOptionsTransformer, ImmutableList.copyOf(builder.transformations));
            this.loggerProxy = builder.loggerProxy;
            this.enabler = builder.enabler;
            this.requireAnnotation = builder.requireAnnotation;
            this.processDoNotMerge = builder.doNotMerge;
            this.descriptionProcessor = builder.moduleDescriptionHandler == null ? m -> {
                ModuleData md = m.getAnnotation(ModuleData.class);
                if (md != null) {
                    return md.description();
                }

                return "";
            } : builder.moduleDescriptionHandler;
            this.headerProcessor = builder.moduleConfigurationHeader == null ? m -> "" : builder.moduleConfigurationHeader;
            this.moduleSection = builder.moduleConfigSection;
            this.moduleSectionHeader = builder.moduleDescription;
            this.allowDisabling = builder.allowDisabling;
        } catch (Exception e) {
            throw new QuickStartModuleDiscoveryException("Unable to start QuickStart", e);
        }

    }

    public final void startDiscover() throws QuickStartModuleDiscoveryException {
        try {
            Preconditions.checkState(currentPhase == ConstructionPhase.INITALISED);
            currentPhase = ConstructionPhase.DISCOVERING;

            Set<Class<? extends M>> modules = discoverModules();
            HashMap<String, ModuleMetadata<? extends M>> discovered = Maps.newHashMap();
            for (Class<? extends M> s : modules) {
                // If we have a module annotation, we are golden.
                String id;
                ModuleMetadata<? extends M> ms;
                if (s.isAnnotationPresent(ModuleData.class)) {
                    ModuleData md = s.getAnnotation(ModuleData.class);
                    id = md.id().toLowerCase();
                    ms = new ModuleMetadata<>(s, this.disableableClass.isAssignableFrom(s), md);
                } else if (this.requireAnnotation) {
                    loggerProxy.warn(MessageFormat.format("The module class {0} does not have a ModuleData annotation associated with it. "
                            + "It is not being loaded as the module container requires the annotation to be present.", s.getName()));
                    continue;
                } else {
                    id = s.getName().toLowerCase();
                    loggerProxy.warn(MessageFormat.format("The module {0} does not have a ModuleData annotation associated with it. We're just assuming an ID of {0}.", id));
                    ms = new ModuleMetadata<>(s, this.disableableClass.isAssignableFrom(s), id, id, LoadingStatus.ENABLED, false);
                }

                if (discovered.containsKey(id)) {
                    throw new QuickStartModuleDiscoveryException("Duplicate module ID \"" + id + "\" was discovered - loading cannot continue.");
                }

                discovered.put(id, ms);
            }

            // Create the dependency map.
            resolveDependencyOrder(discovered);

            // Modules discovered. Create the Module Config adapter.
            List<ModuleMetadata<? extends M>> moduleMetadataList =
                    this.discoveredModules.values().stream()
                            .filter(rModuleMetadata -> !rModuleMetadata.isMandatory())
                            .collect(Collectors.toList());

            // Attaches config adapter and loads in the defaults.
            config.attachModulesConfig(moduleMetadataList, this.descriptionProcessor, this.moduleSection, this.moduleSectionHeader);
            config.saveAdapterDefaults(false);

            // Load what we have in config into our discovered modules.
            try {
                config.getConfigAdapter().getNode().forEach((k, v) -> {
                    try {
                        ModuleMetadata<? extends M> ms = discoveredModules.get(k);
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
        } catch (QuickStartModuleDiscoveryException ex) {
            throw ex;
        } catch (Exception e) {
            throw new QuickStartModuleDiscoveryException("Unable to discover QuickStart modules", e);
        }
    }

    private void resolveDependencyOrder(Map<String, ModuleMetadata<? extends M>> modules) throws Exception {
        // First, get the modules that have no deps.
        processDependencyStep(modules, x -> x.getValue().getDependencies().isEmpty() && x.getValue().getSoftDependencies().isEmpty());

        while (!modules.isEmpty()) {
            Set<String> addedModules = discoveredModules.keySet();
            processDependencyStep(modules, x -> addedModules.containsAll(x.getValue().getDependencies()) && addedModules.containsAll(x.getValue().getSoftDependencies()));
        }
    }

    private void processDependencyStep(Map<String, ModuleMetadata<? extends M>> modules, Predicate<Map.Entry<String, ModuleMetadata<? extends M>>> predicate) {
        // Filter on the predicate
        List<Map.Entry<String, ModuleMetadata<? extends M>>> modulesToAdd = modules.entrySet().stream().filter(predicate)
                .sorted((x, y) -> x.getValue().isMandatory() == y.getValue().isMandatory() ? x.getKey().compareTo(y.getKey()) : Boolean.compare(x.getValue().isMandatory(), y.getValue().isMandatory()))
                .collect(Collectors.toList());

        if (modulesToAdd.isEmpty()) {
            throw new IllegalStateException("Some modules have circular dependencies: " + String.join(", ", modules.keySet()));
        }

        modulesToAdd.forEach(x -> {
            discoveredModules.put(x.getKey(), x.getValue());
            modules.remove(x.getKey());
        });

    }

    private boolean dependenciesSatisfied(ModuleMetadata<? extends M> moduleMetadata, Set<String> enabledModules) {
        if (moduleMetadata.getDependencies().isEmpty()) {
            return true;
        }

        for (String m : moduleMetadata.getDependencies()) {
            if (!enabledModules.contains(m) || !dependenciesSatisfied(this.discoveredModules.get(m), enabledModules)) {
                return false;
            }
        }

        // We know the deps are satisfied.
        return true;
    }

    protected abstract Set<Class<? extends M>> discoverModules() throws Exception;

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
    public Set<String> getDisableableModules() {
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

        ModuleMetadata ms = discoveredModules.get(moduleId);
        if (ms == null) {
            // No module
            throw new NoModuleException(moduleId);
        }

        return ms.getPhase() == ModulePhase.ENABLED;
    }

    /**
     * Requests that a module be disabled. This can only be run during the {@link ConstructionPhase#DISCOVERED} phase, or for
     * {@link Module}s that are disableable (of type {@link D}, {@link ConstructionPhase#ENABLED}.
     *
     * @param moduleName The ID of the module.
     * @throws UndisableableModuleException if the module can't be disabled.
     * @throws NoModuleException if the module does not exist.
     * @throws QuickStartModuleLoaderException if there is a failure during disabling
     */
    public void disableModule(String moduleName) throws UndisableableModuleException, NoModuleException, QuickStartModuleLoaderException {
        if (currentPhase == ConstructionPhase.DISCOVERED) {

            ModuleMetadata ms = discoveredModules.get(moduleName);
            if (ms == null) {
                // No module
                throw new NoModuleException(moduleName);
            }

            if (ms.isMandatory() || ms.getStatus() == LoadingStatus.FORCELOAD) {
                throw new UndisableableModuleException(moduleName);
            }

            ms.setStatus(LoadingStatus.DISABLED);
        } else {
            Preconditions.checkState(currentPhase == ConstructionPhase.ENABLED);
            if (!this.allowDisabling) {
                throw new UndisableableModuleException(moduleName.toLowerCase(), "Cannot disable modules in this holder.");
            }

            ModuleMetadata ms = this.enabledDisableableModules.get(moduleName);
            if (ms == null || !ms.isRuntimeAlterable()) {
                throw new UndisableableModuleException(moduleName.toLowerCase(), "Cannot disable this module at runtime!");
            }

            Preconditions.checkState(ms.getPhase() != ModulePhase.ERRORED, "Cannot disable this module as it errored!");
            Preconditions.checkState(ms.getPhase() == ModulePhase.ENABLED, "Cannot disable this module as it is not enabled!");

            // disableModule(moduleName.toLowerCase());
            D module = this.disableableModules.get(moduleName);
            for (String phase : this.enabler.getDisablePhases()) {
                try {
                    this.enabler.startDisablePhase(phase, this, module);
                } catch (Exception e) {
                    detachConfig(ms.getName());
                    ms.setPhase(ModulePhase.ERRORED);
                    throw new QuickStartModuleLoaderException.Disabling(
                            module.getClass(),
                            "Could not disable the module " + ms.getId(),
                            e
                    );
                }
            }

            detachConfig(ms.getName());
            ms.setPhase(ModulePhase.DISABLED);

            this.enabledModules.remove(moduleName);
            this.enabledDisableableModules.remove(moduleName);
        }
    }

    protected final Class<M> getBaseClass() {
        return this.baseClass;
    }

    /**
     * Get n enabled module given the ID.
     *
     * @param id The ID
     * @param <T> The type, for duck typing
     * @return The module, if it exists.
     */
    @SuppressWarnings("unchecked")
    public <T extends M> Optional<T> getModule(String id) {
        return Optional.ofNullable((T) this.enabledModules.get(id));
    }

    protected M getModule(ModuleMetadata<? extends M> spec) throws Exception {
        M module = this.enabledModules.get(spec.getId());
        if (module == null) {
            return constructModule(spec);
        }

        return module;
    }

    protected abstract M constructModule(ModuleMetadata<? extends M> spec) throws Exception;

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
            List<ModuleMetadata<? extends M>> toDisable = getModules(ModuleStatusTristate.ENABLE)
                    .stream()
                    .map(discoveredModules::get)
                    .filter(x -> !Collections.disjoint(disabledModules, x.getDependencies()))
                    .collect(Collectors.toList());
            if (toDisable.isEmpty()) {
                break;
            }

            if (toDisable.stream().anyMatch(ModuleMetadata::isMandatory)) {
                String s = toDisable.stream().filter(ModuleMetadata::isMandatory).map(ModuleMetadata::getId).collect(Collectors.joining(", "));
                Class<? extends M> m = toDisable.stream().filter(ModuleMetadata::isMandatory).findFirst().get().getModuleClass();
                throw new QuickStartModuleLoaderException.Construction(m,
                        "Tried to disable mandatory module",
                        new IllegalStateException("Dependency failure, tried to disable a mandatory module (" + s + ")"));
            }

            toDisable.forEach(k -> {
                k.setStatus(LoadingStatus.DISABLED);
                disabledModules.add(k.getId());
            });
        }

        // Make sure we get a clean slate here.
        getModules(ModuleStatusTristate.DISABLE).forEach(k -> discoveredModules.get(k).setPhase(ModulePhase.DISABLED));

        // Construct them
        for (String s : getModules(ModuleStatusTristate.ENABLE)) {
            ModuleMetadata<? extends M> ms = discoveredModules.get(s);
            try {
                enabledModules.put(s, constructModule(ms));
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

        if (enabledModules.isEmpty()) {
            currentPhase = ConstructionPhase.ERRORED;
            throw new QuickStartModuleLoaderException.Construction(null, "No modules were constructed.", null);
        }

        enabledModules.forEach((k, v) -> {
            if (this.disableableClass.isAssignableFrom(v.getClass())) {
                this.disableableModules.put(k, this.disableableClass.cast(v));
            }
        });
        int size = enabledModules.size();

        {
            Iterator<Map.Entry<String, M>> im = enabledModules.entrySet().iterator();
            while (im.hasNext()) {
                Map.Entry<String, M> module = im.next();
                try {
                    module.getValue().checkExternalDependencies();
                } catch (MissingDependencyException ex) {
                    this.discoveredModules.get(module.getKey()).setStatus(LoadingStatus.DISABLED);
                    this.discoveredModules.get(module.getKey()).setPhase(ModulePhase.DISABLED);
                    this.loggerProxy.warn("Module " + module.getKey() + " can not be enabled because an external dependency could not be satisfied.");
                    this.loggerProxy.warn("Message was: " + ex.getMessage());
                    im.remove();
                }
            }
        }

        while (size != enabledModules.size()) {
            // We might need to disable modules.
            size = enabledModules.size();
            Iterator<Map.Entry<String, M>> im = enabledModules.entrySet().iterator();
            while (im.hasNext()) {
                Map.Entry<String, M> module = im.next();
                if (!dependenciesSatisfied(this.discoveredModules.get(module.getKey()), getModules(ModuleStatusTristate.ENABLE))) {
                    im.remove();
                    this.loggerProxy.warn("Module " + module.getKey() + " can not be enabled because an external dependency on a module it "
                            + "depends on could not be satisfied.");
                    this.discoveredModules.get(module.getKey()).setStatus(LoadingStatus.DISABLED);
                    this.discoveredModules.get(module.getKey()).setPhase(ModulePhase.DISABLED);
                }
            }

        }

        // Enter Config Adapter phase - attaching before enabling so that enable methods can get any associated configurations.
        for (String s : enabledModules.keySet()) {
            M m = enabledModules.get(s);
            try {
                attachConfig(s, m);
            } catch (Exception e) {
                e.printStackTrace();
                if (failOnOneError) {
                    throw new QuickStartModuleLoaderException.Enabling(m.getClass(), "Failed to attach config.", e);
                }
            }
        }

        // Enter Enable phase.
        Set<String> phases = this.enabler.getEnablePhases();

        for (String phase : phases) {
            loggerProxy.info(String.format("Starting phase: %s", phase));
            try {
                this.enabler.startEnablePrePhase(phase, this);
            } catch (Exception ex) {
                this.currentPhase = ConstructionPhase.ERRORED;
                throw new RuntimeException("Could not load modules, phase " + phase + " failed to load.", ex);
            }
            Iterator<String> is = enabledModules.keySet().iterator();
            while (is.hasNext()) {
                String i = is.next();
                ModuleMetadata<? extends M> ms = discoveredModules.get(i);

                // If the module is errored, then we do not continue.
                if (ms.getPhase() == ModulePhase.ERRORED) {
                    continue;
                }

                try {
                    M m = enabledModules.get(i);
                    this.enabler.startEnablePhase(phase, this, m);
                } catch (Exception construction) {
                    construction.printStackTrace();
                    is.remove();

                    ms.setPhase(ModulePhase.ERRORED);
                    loggerProxy.error("The module " + ms.getModuleClass().getName() + " failed to enable.");

                    if (failOnOneError) {
                        currentPhase = ConstructionPhase.ERRORED;
                        throw new QuickStartModuleLoaderException.Enabling(ms.getModuleClass(), "The module " + ms.getModuleClass().getName() + " failed to enable.", construction);
                    }
                }
            }
        }

        if (enabledModules.isEmpty()) {
            currentPhase = ConstructionPhase.ERRORED;
            throw new QuickStartModuleLoaderException.Enabling(null, "No modules were enabled.", null);
        }

        // Modules in this list did not fail.
        enabledModules.forEach((k, v) -> this.discoveredModules.get(k).setPhase(ModulePhase.ENABLED));
        resetDisableableList();
        try {
            config.saveAdapterDefaults(this.processDoNotMerge);
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentPhase = ConstructionPhase.ENABLED;
    }

    private void resetDisableableList() {
        this.enabledDisableableModules.clear();
        this.discoveredModules.values().stream()
                .filter(x -> x.getPhase() == ModulePhase.ENABLED)
                .filter(ModuleMetadata::isRuntimeAlterable)
                .forEach(x -> this.enabledDisableableModules.put(x.getId(), (ModuleMetadata<? extends D>) x));
    }

    /**
     * Enables a {@link D} after the construction has completed.
     *
     * @param name The name of the module to load.
     * @throws Exception thrown if the module is not loadable for any reason, including if it is already enabled.
     */
    public void runtimeEnable(String name) throws Exception {
        runtimeEnable(ImmutableSet.of(name));
    }

    public void runtimeEnable(Set<String> name) throws Exception {
        Preconditions.checkState(this.currentPhase == ConstructionPhase.ENABLED);

        Set<String> modulesToCheck = name.stream().map(String::toLowerCase).collect(Collectors.toSet());

        Set<ModuleMetadata<? extends D>> containers = new HashSet<>();
        for (String moduleName : modulesToCheck) {
            Preconditions.checkState(!isModuleLoaded(moduleName), "Module is already loaded!");
            ModuleMetadata<? extends M> ms = discoveredModules.get(moduleName);
            Preconditions.checkState(this.disableableClass.isAssignableFrom(ms.getModuleClass()),
                    "Module " + name + " cannot be enabled at runtime!");

            //noinspection unchecked
            containers.add((ModuleMetadata<? extends D>) ms);
        }

        for (ModuleMetadata<? extends D> ms : containers) {
            try {
                // Construction
                D module = this.disableableModules.get(ms.getId());
                if (module == null) {
                    module = (D) constructModule(ms);
                    this.disableableModules.put(ms.getId(), module);
                }

                ms.setPhase(ModulePhase.CONSTRUCTED);
                Set<String> phases = this.enabler.getEnablePhases();

                module.checkExternalDependencies();

                // Enabling
                for (String phase : phases) {
                    this.enabler.startEnablePhase(phase, this, module);
                }

                ms.setPhase(ModulePhase.ENABLED);
                this.enabledModules.put(ms.getId(), module);
            } catch (Exception construction) {
                ms.setPhase(ModulePhase.ERRORED);
                throw construction;
            }
        }


        resetDisableableList();
    }

    private void attachConfig(String name, M m) throws Exception {
        Optional<AbstractConfigAdapter<?>> a = m.getConfigAdapter();
        if (a.isPresent()) {
            config.attachConfigAdapter(name, a.get(), this.headerProcessor.apply(m));
        }
    }

    private void detachConfig(String name) {
        config.detachConfigAdapter(name);
    }

    @SuppressWarnings("unchecked")
    public final <C extends AbstractConfigAdapter<?>> C getConfigAdapterForModule(String module,
            Class<C> adapterClass) throws NoModuleException, IncorrectAdapterTypeException {
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
     * Gets the registered module ID, if it exists.
     *
     * @param module The module.
     * @return The module ID, or an empty {@link Optional#empty()}
     */
    public final Optional<String> getIdForModule(Module module) {
        return discoveredModules.entrySet().stream().filter(x -> x.getValue().getModuleClass() == module.getClass()).map(Map.Entry::getKey).findFirst();
    }

    /**
     * Builder class to create a {@link ModuleHolder}
     */
    public static abstract class Builder<M extends Module, D extends M, R extends ModuleHolder<M, D>, T extends Builder<M, D, R, T>> {

        boolean allowDisabling = false;
        final Class<M> moduleType;
        final Class<D> disableableClass;
        PhasedModuleEnabler<M, D> enabler;
        ConfigurationLoader<? extends ConfigurationNode> configurationLoader;
        boolean requireAnnotation = false;
        LoggerProxy loggerProxy;
        final List<AbstractConfigAdapter.Transformation> transformations = new ArrayList<>();
        Function<ConfigurationOptions, ConfigurationOptions> configurationOptionsTransformer = x -> x;
        boolean doNotMerge = false;
        @Nullable Function<Class<? extends M>, String> moduleDescriptionHandler = null;
        @Nullable Function<M, String> moduleConfigurationHeader = null;
        String moduleConfigSection = "modules";
        @Nullable String moduleDescription = null;

        protected abstract T getThis();

        /**
         * Creates a builder with the given type of {@link Module}.
         *
         * @param moduleType The type of module.
         * @param disableableClass The type of disableable module, which extends {@link M}
         */
        public Builder(Class<M> moduleType, Class<D> disableableClass) {
            this.moduleType = moduleType;
            this.disableableClass = disableableClass;
        }

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
         * Sets the {@link PhasedModuleEnabler} to run when enabling modules.
         *
         * @param enabler The {@link PhasedModuleEnabler}
         * @return This {@link Builder}, for chaining.
         */
        public T setModuleEnabler(PhasedModuleEnabler<M, D> enabler) {
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

        /**
         * Sets whether {@link TypedAbstractConfigAdapter} {@link ConfigSerializable} fields that have the annotation {@link NoMergeIfPresent}
         * will <em>not</em> be merged into existing config values.
         *
         * @param noMergeIfPresent <code>true</code> if fields should be skipped if they are already populated.
         * @return This {@link Builder}, for chaining.
         */
        public T setNoMergeIfPresent(boolean noMergeIfPresent) {
            this.doNotMerge = noMergeIfPresent;
            return getThis();
        }

        /**
         * Sets the function that is used to set the description for each module in the configuration file.
         *
         * <p>
         *     This is displayed above each of the module toggles in the configuration file.
         * </p>
         *
         * @param handler The {@link Function} to use, or {@code null} otherwise.
         * @return This {@link Builder}, for chaining.
         */
        public T setModuleDescriptionHandler(@Nullable Function<Class<? extends M>, String> handler) {
            this.moduleDescriptionHandler = handler;
            return getThis();
        }

        /**
         * Sets the function that is used to set the header for each module's configuration block in the configuration file.
         *
         * <p>
         *     This is displayed above each of the configuration sections in the configuration file.
         * </p>
         *
         * @param header The {@link Function} to use, or {@code null} otherwise.
         * @return This {@link Builder}, for chaining.
         */
        public T setModuleConfigurationHeader(@Nullable Function<M, String> header) {
            this.moduleConfigurationHeader = header;
            return getThis();
        }

        /**
         * Sets the name of the section that contains the module enable/disable flags.
         *
         * @param name The name of the section. Defaults to "modules"
         * @return This {@link Builder}, for chaining.
         */
        public T setModuleConfigSectionName(String name) {
            Preconditions.checkNotNull(name);
            this.moduleConfigSection = name;
            return getThis();
        }

        /**
         * Sets the description for the module config section.
         *
         * @param description The description, or {@code null} to use the default.
         * @return This {@link Builder}, for chaining.
         */
        public T setModuleConfigSectionDescription(@Nullable String description) {
            this.moduleDescription = description;
            return getThis();
        }

        /**
         * Tells the system how to transform the entire configuration before it
         * is loaded. Multiple transformations can be performed by chaining this
         * method.
         *
         * @param transformation The transformation to apply.
         * @return This {@link Builder}, for chaining.
         */
        public T transformConfig(AbstractConfigAdapter.Transformation transformation) {
            this.transformations.add(transformation);
            return getThis();
        }

        /**
         * Sets whether modules in this module holder can be disabled at runtime.
         *
         * @param allowDisable true if so
         * @return This {@link Builder}, for chaining.
         */
        public T setAllowDisable(boolean allowDisable) {
            this.allowDisabling = allowDisable;
            return getThis();
        }

        protected void checkBuild() {
            Preconditions.checkNotNull(configurationLoader);
            Preconditions.checkNotNull(moduleConfigSection);
            Preconditions.checkNotNull(enabler);

            if (loggerProxy == null) {
                loggerProxy = DefaultLogger.INSTANCE;
            }

            Metadata.getStartupMessage().ifPresent(x -> loggerProxy.info(x));
        }

        public abstract R build() throws Exception;

        /**
         * Builds the module container and immediately starts discovery.
         *
         * @param startDiscover <code>true</code> if so.
         * @return The built module container.
         * @throws Exception if there was a problem during building or discovery.
         */
        public final R build(boolean startDiscover) throws Exception {
            R build = build();
            if (startDiscover) {
                build.startDiscover();
            }

            return build;
        }
    }

    public enum ModuleStatusTristate {
        ENABLE(k -> k.getValue().getStatus() != LoadingStatus.DISABLED && k.getValue().getPhase() != ModulePhase.ERRORED && k.getValue().getPhase() != ModulePhase.DISABLED),
        DISABLE(k -> !ENABLE.statusPredicate.test(k)),
        ALL(k -> true);

        private final Predicate<Map.Entry<String, ? extends ModuleMetadata<? extends Module>>> statusPredicate;

        ModuleStatusTristate(Predicate<Map.Entry<String, ? extends ModuleMetadata<? extends Module>>> p) {
            statusPredicate = p;
        }
    }

}
