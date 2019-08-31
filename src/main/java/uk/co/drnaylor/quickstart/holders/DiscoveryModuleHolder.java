/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.holders;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.ModuleMetadata;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.holders.discoverystrategies.Strategy;
import uk.co.drnaylor.quickstart.loaders.ModuleConstructor;
import uk.co.drnaylor.quickstart.loaders.SimpleModuleConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Discovery module container tries to load and instantiate modules that are
 * discovered using reflection. It requires a root package to scan - it will
 * then use a discovery strategy to load all classes and from there, pick out
 * the modules.
 *
 * <p>All classes that were discovered by the module container are available
 * in the container, to save users from multiple classpath scans.</p>
 *
 * @param <M> The type of {@link Module} that this contains.
 * @param <D> The type of {@link Module} that is disableable.
 */
public final class DiscoveryModuleHolder<M extends Module, D extends M> extends ModuleHolder<M, D> {

    /**
     * Gets a builder to create a {@link DiscoveryModuleHolder}
     *
     * @param moduleClass The {@link Class} of type {@link M} that represents the type of module
     *      that this holder will contain.
     * @param disableClass The {@link Class} of type {@link D} that represents the type of module
     *      that this holder will contain and is disableable.
     * @param <M> The type of module.
     * @param <D> The type of module that is disableable.
     * @return A {@link DiscoveryModuleHolder.Builder} for building a {@link DiscoveryModuleHolder}
     */
    public static <M extends Module, D extends M> DiscoveryModuleHolder.Builder<M, D> builder(Class<M> moduleClass, Class<D> disableClass) {
        return new DiscoveryModuleHolder.Builder<>(moduleClass, disableClass);
    }

    /**
     * The {@link ClassLoader} to scan for new modules.
     */
    private final ClassLoader classLoader;

    /**
     * Provides the methods to use to construct the module.
     */
    private final ModuleConstructor<M> constructor;

    /**
     * The root of the package to scan.
     */
    private final String packageLocation;

    /**
     * The classes that were loaded by the module loader.
     */
    private final Set<Class<?>> loadedClasses = new HashSet<>();

    /**
     * The strategy for loading classes on the classpath.
     */
    private final Strategy strategy;

    /**
     * Constructs a {@link ModuleHolder} and starts discovery of the modules.
     *
     * @param builder The builder to build this from.
     */
    private DiscoveryModuleHolder(DiscoveryModuleHolder.Builder<M, D> builder) throws QuickStartModuleDiscoveryException {
        super(builder);
        this.classLoader = builder.classLoader;
        this.constructor = builder.constructor;
        this.packageLocation = builder.packageToScan;
        this.strategy = builder.strategy;
    }

    /**
     * Starts discovery of modules.
     */
    @Override
    protected Set<Class<? extends M>> discoverModules() throws Exception {
        // Get the modules out.
        loadedClasses.addAll(this.strategy.discover(packageLocation, classLoader));
        final Class<M> basicClass = getBaseClass();
        Set<Class<? extends M>> modules = loadedClasses.stream().filter(basicClass::isAssignableFrom)
                .map(x -> (Class<? extends M>) x.asSubclass(basicClass)).collect(Collectors.toSet());

        if (modules.isEmpty()) {
            throw new QuickStartModuleDiscoveryException("No modules were found", null);
        }

        return modules;
    }

    @Override
    protected M constructModule(ModuleMetadata<? extends M> spec) throws Exception {
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

    public final static class Builder<M extends Module, D extends M>
            extends ModuleHolder.Builder<M, D, DiscoveryModuleHolder<M, D>, Builder<M, D>> {
        private String packageToScan;
        private ModuleConstructor<M> constructor = new SimpleModuleConstructor<>();
        private ClassLoader classLoader;
        private Strategy strategy = Strategy.DEFAULT;

        /**
         * Creates a builder with the given type of {@link Module}.
         *
         * @param moduleType The type of module.
         */
        Builder(Class<M> moduleType, Class<D> disableType) {
            super(moduleType, disableType);
        }

        /**
         * Sets the root package name to scan.
         *
         * @param packageToScan The root of the package (for example, <code>uk.co.drnaylor.quickstart</code> will scan
         *                      that package and all subpackages, such as <code>uk.co.drnaylor.quickstart.config</code>)
         * @return This {@link ModuleHolder.Builder}, for chaining.
         */
        public Builder<M, D> setPackageToScan(String packageToScan) {
            this.packageToScan = packageToScan;
            return this;
        }

        /**
         * Sets the {@link ModuleConstructor} to use when building the module objects.
         *
         * @param constructor The constructor to use
         * @return This {@link ModuleHolder.Builder}, for chaining.
         */
        public Builder<M, D> setConstructor(ModuleConstructor<M> constructor) {
            this.constructor = constructor;
            return this;
        }

        /**
         * Sets the {@link ClassLoader} to use when scanning the classpath.
         *
         * @param classLoader The class loader to use.
         * @return This {@link ModuleHolder.Builder}, for chaining.
         */
        public Builder<M, D> setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Sets the {@link Strategy} for this container.
         *
         * <p>The strategy consumes the package to scan and the
         * {@link ClassLoader}, and returns a {@link Set} of
         * {@link Class}es.</p>
         *
         * @param strategy The strategy to use
         * @return This {@link ModuleHolder.Builder}, for chaining.
         */
        public Builder<M, D> setStrategy(Strategy strategy) {
            this.strategy = Preconditions.checkNotNull(strategy);
            return this;
        }

        @Override
        protected Builder<M, D> getThis() {
            return this;
        }

        /**
         * Builds a {@link ModuleHolder}.
         *
         * @return The {@link ModuleHolder}.
         * @throws QuickStartModuleDiscoveryException if the configuration loader cannot load data from the file.
         */
        public DiscoveryModuleHolder<M, D> build() throws QuickStartModuleDiscoveryException {
            Preconditions.checkNotNull(packageToScan);

            if (constructor == null) {
                constructor = new SimpleModuleConstructor<>();
            }

            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            checkBuild();
            return new DiscoveryModuleHolder<>(this);
        }
    }

}
