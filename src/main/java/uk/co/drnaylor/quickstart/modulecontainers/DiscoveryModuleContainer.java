/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.modulecontainers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import uk.co.drnaylor.quickstart.*;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.loaders.ModuleConstructor;
import uk.co.drnaylor.quickstart.loaders.SimpleModuleConstructor;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.loaders.ModuleEnabler;

import java.text.MessageFormat;
import java.util.Set;
import java.util.stream.Collectors;

public class DiscoveryModuleContainer extends ModuleContainer {

    /**
     * Gets a builder to create a {@link DiscoveryModuleContainer}
     *
     * @return A {@link DiscoveryModuleContainer.Builder} for building a {@link DiscoveryModuleContainer}
     */
    public static DiscoveryModuleContainer.Builder builder() {
        return new DiscoveryModuleContainer.Builder();
    }

    /**
     * The {@link ClassLoader} to scan for new modules.
     */
    private final ClassLoader classLoader;

    /**
     * Provides the methods to use to construct the module.
     */
    private final ModuleConstructor constructor;

    /**
     * The root of the package to scan.
     */
    private final String packageLocation;

    /**
     * The classes that were loaded by the module loader.
     */
    private final Set<Class<?>> loadedClasses = Sets.newHashSet();

    /**
     * Constructs a {@link ModuleContainer} and starts discovery of the modules.
     *
     * @param configurationLoader The {@link ConfigurationLoader} that contains details of whether the modules should be enabled or not.
     * @param loader              The {@link ClassLoader} that contains the classpath in which the modules are located.
     * @param packageBase         The root name of the package to scan for modules.
     * @param constructor         The {@link ModuleConstructor} that contains the logic to construct modules.
     * @param enabler             The {@link ModuleEnabler} that contains the logic to enable modules.
     * @param loggerProxy         The {@link LoggerProxy} that contains methods to send messages to the logger, or any other source.
     * @param onPreEnable         The {@link Procedure} to run on pre enable, before modules are pre-enabled.
     * @param onEnable            The {@link Procedure} to run on enable, before modules are pre-enabled.
     * @param onPostEnable        The {@link Procedure} to run on post enable, before modules are pre-enabled.
     * @throws QuickStartModuleDiscoveryException if there is an error starting the Module Container.
     */
    private <N extends ConfigurationNode> DiscoveryModuleContainer(ConfigurationLoader<N> configurationLoader, ClassLoader loader, String packageBase, ModuleConstructor constructor, ModuleEnabler enabler, LoggerProxy loggerProxy, Procedure onPreEnable, Procedure onEnable, Procedure onPostEnable) throws QuickStartModuleDiscoveryException {
        super(configurationLoader, loggerProxy, enabler, onPreEnable, onEnable, onPostEnable);
        this.classLoader = loader;
        this.constructor = constructor;
        this.packageLocation = packageBase;
    }

    /**
     * Starts discovery of modules.
     */
    @Override
    protected Set<Class<? extends Module>> discoverModules() throws Exception {
        // Get the modules out.
        Set<ClassPath.ClassInfo> ci = ClassPath.from(classLoader).getTopLevelClassesRecursive(packageLocation);
        loadedClasses.addAll(ci.stream().map(ClassPath.ClassInfo::load).collect(Collectors.toSet()));
        Set<Class<? extends Module>> modules = loadedClasses.stream().filter(Module.class::isAssignableFrom)
                .map(x -> (Class<? extends Module>)x.asSubclass(Module.class)).collect(Collectors.toSet());

        if (modules.isEmpty()) {
            throw new QuickStartModuleDiscoveryException("No modules were found", null);
        }

        return modules;
    }

    @Override
    protected Module getModule(ModuleSpec spec) throws Exception {
        return constructor.constructModule(spec.getModuleClass());
    }


    /**
     * Gets the {@link Class}es that were scanned during the module discovery phase.
     *
     * @return Gets a {@link Set} of the loaded classes.
     */
    public final Set<Class<?>> getLoadedClasses() {
        return ImmutableSet.copyOf(this.loadedClasses);
    }

    public final static class Builder extends ModuleContainer.Builder<DiscoveryModuleContainer, Builder> {
        private String packageToScan;
        private ModuleConstructor constructor = SimpleModuleConstructor.INSTANCE;
        private ClassLoader classLoader;

        /**
         * Sets the root package name to scan.
         *
         * @param packageToScan The root of the package (for example, <code>uk.co.drnaylor.quickstart</code> will scan
         *                      that package and all subpackages, such as <code>uk.co.drnaylor.quickstart.config</code>)
         * @return This {@link ModuleContainer.Builder}, for chaining.
         */
        public Builder setPackageToScan(String packageToScan) {
            this.packageToScan = packageToScan;
            return this;
        }

        /**
         * Sets the {@link ModuleConstructor} to use when building the module objects.
         *
         * @param constructor The constructor to use
         * @return This {@link ModuleContainer.Builder}, for chaining.
         */
        public Builder setConstructor(ModuleConstructor constructor) {
            this.constructor = constructor;
            return this;
        }

        /**
         * Sets the {@link ClassLoader} to use when scanning the classpath.
         *
         * @param classLoader The class loader to use.
         * @return This {@link ModuleContainer.Builder}, for chaining.
         */
        public Builder setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        /**
         * Builds a {@link ModuleContainer}.
         *
         * @return The {@link ModuleContainer}.
         * @throws QuickStartModuleDiscoveryException if the configuration loader cannot load data from the file.
         */
        public DiscoveryModuleContainer build() throws QuickStartModuleDiscoveryException {
            Preconditions.checkNotNull(packageToScan);

            if (constructor == null) {
                constructor = SimpleModuleConstructor.INSTANCE;
            }

            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            checkBuild();
            return new DiscoveryModuleContainer(configurationLoader, classLoader, packageToScan, constructor, enabler, loggerProxy,
                    onPreEnable, onEnable, onPostEnable);
        }
    }
}
