/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.holders;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import uk.co.drnaylor.quickstart.LoggerProxy;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.ModuleSpec;
import uk.co.drnaylor.quickstart.Procedure;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.NoMergeIfPresent;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.loaders.ModuleConstructor;
import uk.co.drnaylor.quickstart.loaders.ModuleEnabler;
import uk.co.drnaylor.quickstart.loaders.SimpleModuleConstructor;
import uk.co.drnaylor.quickstart.holders.discoverystrategies.Strategy;
import uk.co.drnaylor.quickstart.util.ThrownBiFunction;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * The Discovery module container tries to load and instantiate modules that are
 * discovered using reflection. It requires a root package to scan - it will
 * then use a discovery strategy to load all classes and from there, pick out
 * the modules.
 *
 * <p>All classes that were discovered by the module container are available
 * in the container, to save users from multiple classpath scans.</p>
 */
public final class DiscoveryModuleHolder extends ModuleHolder {

    /**
     * Gets a builder to create a {@link DiscoveryModuleHolder}
     *
     * @return A {@link DiscoveryModuleHolder.Builder} for building a {@link DiscoveryModuleHolder}
     */
    public static DiscoveryModuleHolder.Builder builder() {
        return new DiscoveryModuleHolder.Builder();
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
     * The strategy for loading classes on the classpath.
     */
    private final Strategy strategy;

    /**
     * Constructs a {@link ModuleHolder} and starts discovery of the modules.
     *
     * @param builder The builder to build this from.
     * @param <N> The type of {@link ConfigurationNode} to use.
     */
    private <N extends ConfigurationNode> DiscoveryModuleHolder(DiscoveryModuleHolder.Builder builder) throws QuickStartModuleDiscoveryException {
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
    protected Set<Class<? extends Module>> discoverModules() throws Exception {
        // Get the modules out.
        loadedClasses.addAll(this.strategy.discover(packageLocation, classLoader));

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

    public final static class Builder extends ModuleHolder.Builder<DiscoveryModuleHolder, Builder> {
        private String packageToScan;
        private ModuleConstructor constructor = SimpleModuleConstructor.INSTANCE;
        private ClassLoader classLoader;
        private Strategy strategy = Strategy.DEFAULT;

        /**
         * Sets the root package name to scan.
         *
         * @param packageToScan The root of the package (for example, <code>uk.co.drnaylor.quickstart</code> will scan
         *                      that package and all subpackages, such as <code>uk.co.drnaylor.quickstart.config</code>)
         * @return This {@link ModuleHolder.Builder}, for chaining.
         */
        public Builder setPackageToScan(String packageToScan) {
            this.packageToScan = packageToScan;
            return this;
        }

        /**
         * Sets the {@link ModuleConstructor} to use when building the module objects.
         *
         * @param constructor The constructor to use
         * @return This {@link ModuleHolder.Builder}, for chaining.
         */
        public Builder setConstructor(ModuleConstructor constructor) {
            this.constructor = constructor;
            return this;
        }

        /**
         * Sets the {@link ClassLoader} to use when scanning the classpath.
         *
         * @param classLoader The class loader to use.
         * @return This {@link ModuleHolder.Builder}, for chaining.
         */
        public Builder setClassLoader(ClassLoader classLoader) {
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
        public Builder setDiscoveryStrategy(ThrownBiFunction<String, ClassLoader, Set<Class<?>>, Exception> strategy) {
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
        public Builder setStrategy(Strategy strategy) {
            this.strategy = Preconditions.checkNotNull(strategy);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        /**
         * Builds a {@link ModuleHolder}.
         *
         * @return The {@link ModuleHolder}.
         * @throws QuickStartModuleDiscoveryException if the configuration loader cannot load data from the file.
         */
        public DiscoveryModuleHolder build() throws QuickStartModuleDiscoveryException {
            Preconditions.checkNotNull(packageToScan);

            if (constructor == null) {
                constructor = SimpleModuleConstructor.INSTANCE;
            }

            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            checkBuild();
            return new DiscoveryModuleHolder(this);
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
