/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.holders;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.ModuleSpec;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.holders.discoverystrategies.Strategy;
import uk.co.drnaylor.quickstart.loaders.ModuleConstructor;
import uk.co.drnaylor.quickstart.loaders.SimpleModuleConstructor;
import uk.co.drnaylor.quickstart.util.ThrownBiFunction;

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
 */
public final class DiscoveryModuleHolder<M extends Module> extends ModuleHolder<M> {

    /**
     * Gets a builder to create a {@link DiscoveryModuleHolder}
     *
     * @param moduleClass The {@link Class} of type {@link M} that represents the type of module
     *      that this holder will contain.
     * @param <M> The type of module.
     * @return A {@link DiscoveryModuleHolder.Builder} for building a {@link DiscoveryModuleHolder}
     */
    public static <M extends Module> DiscoveryModuleHolder.Builder<M> builder(Class<M> moduleClass) {
        return new DiscoveryModuleHolder.Builder<>(moduleClass);
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
    private final Set<Class<?>> loadedClasses = Sets.newHashSet();

    /**
     * The strategy for loading classes on the classpath.
     */
    private final Strategy strategy;

    /**
     * Constructs a {@link ModuleHolder} and starts discovery of the modules.
     *
     * @param builder The builder to build this from.
     */
    private DiscoveryModuleHolder(DiscoveryModuleHolder.Builder<M> builder) throws QuickStartModuleDiscoveryException {
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
    protected M getModule(ModuleSpec<M> spec) throws Exception {
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

    public final static class Builder<M extends Module> extends ModuleHolder.Builder<M, DiscoveryModuleHolder<M>, Builder<M>> {
        private String packageToScan;
        private ModuleConstructor<M> constructor = new SimpleModuleConstructor<>();
        private ClassLoader classLoader;
        private Strategy strategy = Strategy.DEFAULT;

        /**
         * Creates a builder with the given type of {@link Module}.
         *
         * @param moduleType The type of module.
         */
        Builder(Class<M> moduleType) {
            super(moduleType);
        }

        /**
         * Sets the root package name to scan.
         *
         * @param packageToScan The root of the package (for example, <code>uk.co.drnaylor.quickstart</code> will scan
         *                      that package and all subpackages, such as <code>uk.co.drnaylor.quickstart.config</code>)
         * @return This {@link ModuleHolder.Builder}, for chaining.
         */
        public Builder<M> setPackageToScan(String packageToScan) {
            this.packageToScan = packageToScan;
            return this;
        }

        /**
         * Sets the {@link ModuleConstructor} to use when building the module objects.
         *
         * @param constructor The constructor to use
         * @return This {@link ModuleHolder.Builder}, for chaining.
         */
        public Builder<M> setConstructor(ModuleConstructor<M> constructor) {
            this.constructor = constructor;
            return this;
        }

        /**
         * Sets the {@link ClassLoader} to use when scanning the classpath.
         *
         * @param classLoader The class loader to use.
         * @return This {@link ModuleHolder.Builder}, for chaining.
         */
        public Builder<M> setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Sets the {@link DiscoveryStrategy} for this container.
         *
         * <p>The strategy consumes the package to scan and the
         * {@link ClassLoader}, and returns a {@link Set} of
         * {@link Class}es.</p>
         *
         * @param strategy The strategy to use
         * @return This {@link ModuleHolder.Builder}, for chaining.
         * @deprecated Use {@link #setStrategy(Strategy)} instead.
         */
        @Deprecated
        public Builder<M> setDiscoveryStrategy(ThrownBiFunction<String, ClassLoader, Set<Class<?>>, Exception> strategy) {
            Preconditions.checkNotNull(strategy);
            this.strategy = strategy::apply;
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
        public Builder<M> setStrategy(Strategy strategy) {
            this.strategy = Preconditions.checkNotNull(strategy);
            return this;
        }

        @Override
        protected Builder<M> getThis() {
            return this;
        }

        /**
         * Builds a {@link ModuleHolder}.
         *
         * @return The {@link ModuleHolder}.
         * @throws QuickStartModuleDiscoveryException if the configuration loader cannot load data from the file.
         */
        public DiscoveryModuleHolder<M> build() throws QuickStartModuleDiscoveryException {
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

    /**
     * Determines the strategy used to discover modules.
     *
     * @deprecated Use {@link Strategy}
     */
    @Deprecated
    public enum DiscoveryStrategy implements ThrownBiFunction<String, ClassLoader, Set<Class<?>>, Exception> {

        /**
         * The default in-built strategy uses the Google reflect
         * library.
         */
        @Deprecated
        DEFAULT {
            @Override public Set<Class<?>> apply(String s, ClassLoader classLoader) throws Exception {
                return Strategy.DEFAULT.discover(s, classLoader);
            }
        },

        /**
         * Discovers classes using the DEFAULT. Does not use the Fast Classpath Scanner any more.
         */
        @Deprecated
        FAST_CLASSPATH_SCANNER {
            @Override public Set<Class<?>> apply(String s, ClassLoader classLoader) throws Exception {
                return Strategy.DEFAULT.discover(s, classLoader);
            }
        },

        /**
         * Uses Google Reflect to discover classes
         */
        @Deprecated
        GOOGLE_REFLECT {
            @Override public Set<Class<?>> apply(String s, ClassLoader classLoader) throws Exception {
                return Strategy.DEFAULT.discover(s, classLoader);
            }
        }

    }

}
