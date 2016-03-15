/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.ModulesConfigAdapter;
import uk.co.drnaylor.quickstart.constructors.ModuleConstructor;
import uk.co.drnaylor.quickstart.constructors.SimpleModuleConstructor;
import uk.co.drnaylor.quickstart.enums.ConstructionPhase;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
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
    private ConstructionPhase currentPhase = ConstructionPhase.DISCOVERING;

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

    private final Consumer<Class<? extends Module>> modulePopulator = s -> {
        // If we have a module annotation, we are golden.
        if (s.isAnnotationPresent(ModuleData.class)) {
            ModuleData md = s.getAnnotation(ModuleData.class);
            discoveredModules.put(md.id().toLowerCase(), new ModuleSpec(s, md));
        } else {
            String id = s.getName().toLowerCase();
            Logger.getLogger("QuickStart Module Loader").warning(MessageFormat.format("The module {0} does not have a ModuleData annotation associated with it. We're just assuming an ID of {0}.", id));
            discoveredModules.put(id, new ModuleSpec(s, id, LoadingStatus.ENABLED));
        }
    };

    /**
     * Constructs a {@link ModuleContainer} and starts discovery of the modules.
     *
     * @param configurationLoader The {@link ConfigurationLoader} that contains details of whether the modules should be enabled or not.
     * @param loader The {@link ClassLoader} that contains the classpath in which the modules are located.
     * @param packageBase The root name of the package to scan for modules.
     */
    private ModuleContainer(ConfigurationLoader<? extends ConfigurationNode> configurationLoader, ClassLoader loader, String packageBase, ModuleConstructor constructor) throws IOException {
        Preconditions.checkNotNull(configurationLoader);
        Preconditions.checkNotNull(loader);
        Preconditions.checkNotNull(packageBase);
        Preconditions.checkNotNull(constructor);

        this.config = new SystemConfig<>(configurationLoader);
        this.constructor = constructor;
        this.classLoader = loader;
        this.packageLocation = packageBase;

        discoverModules();
    }

    /**
     * Starts discovery of modules.
     */
    private void discoverModules() throws IOException {
        Preconditions.checkArgument(currentPhase == ConstructionPhase.INITALISED);
        currentPhase = ConstructionPhase.DISCOVERING;

        // Get the modules out.
        Set<ClassPath.ClassInfo> ci = ClassPath.from(classLoader).getTopLevelClassesRecursive(packageLocation);
        Set<Class<? extends Module>> modules = ci.stream().map(ClassPath.ClassInfo::load).map(x -> x.asSubclass(Module.class)).collect(Collectors.toSet());

        // Put the modules into the discoverer.
        modules.forEach(modulePopulator);

        // Modules discovered. Create the Module Config adapter.
        Map<String, LoadingStatus> m = discoveredModules.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getStatus()));
        config.attachConfigAdapter(ModulesConfigAdapter.modulesKey, new ModulesConfigAdapter<>(m));

        currentPhase = ConstructionPhase.DISCOVERED;
    }

    /**
     * Starts the module enabling phase.
     */
    public void startModuleConstruction() {
        Preconditions.checkArgument(currentPhase == ConstructionPhase.DISCOVERED);
        currentPhase = ConstructionPhase.ENABLING;
    }

    /**
     * Builder class to create a {@link ModuleContainer}
     */
    public static class Builder {

        private ConfigurationLoader<? extends ConfigurationNode> configurationLoader;
        private String packageToScan;
        private ModuleConstructor constructor;
        private ClassLoader classLoader;

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
         * Builds a {@link ModuleContainer}.
         *
         * @return The {@link ModuleContainer}.
         * @throws IOException if the configuration loader cannot load data from the file.
         */
        public ModuleContainer build() throws IOException {
            Preconditions.checkNotNull(packageToScan);
            Preconditions.checkNotNull(configurationLoader);

            if (constructor == null) {
                constructor = SimpleModuleConstructor.INSTANCE;
            }

            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            return new ModuleContainer(configurationLoader, classLoader, packageToScan, constructor);
        }
    }
}
