/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.holders;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.ModuleSpec;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;

import java.util.Map;
import java.util.Set;

/**
 * The provided module container is provided pre-constructed {@link Module}s to work with.
 */
public final class ProvidedModuleHolder<M extends Module> extends ModuleHolder<M> {

    public static <M extends Module> Builder<M> builder(Class<M> moduleClass) {
        return new Builder<>(moduleClass);
    }

    private final Map<Class<? extends M>, M> moduleMap;

    private ProvidedModuleHolder(Builder<M> builder) throws QuickStartModuleDiscoveryException {
        super(builder);
        ImmutableMap.Builder<Class<? extends M>, M> mapBuilder = ImmutableMap.builder();
        for (M module : builder.modules) {
            mapBuilder.put((Class<? extends M>) module.getClass(), module);
        }
        this.moduleMap = mapBuilder.build();
    }

    @Override
    protected Set<Class<? extends M>> discoverModules() {
        // They've kind of been discovered.
        return moduleMap.keySet();
    }

    @Override
    protected M getModule(ModuleSpec spec) throws Exception {
        M module = moduleMap.get(spec.getModuleClass());
        if (module == null) {
            throw new NoModuleException(spec.getName());
        }

        return module;
    }

    public static class Builder<M extends Module> extends ModuleHolder.Builder<M, ProvidedModuleHolder<M>, Builder<M>> {

        /**
         * The set of module to load.
         */
        private Set<M> modules;

        /**
         * Creates a builder with the given type of {@link Module}.
         *
         * @param moduleType The type of module.
         */
        Builder(Class<M> moduleType) {
            super(moduleType);
        }

        /**
         * Sets the {@link Set} of {@link Module}s that QSML should load.
         *
         * @param modules The set of moudules to load.
         * @return This {@link Builder}, for chaining.
         */
        public Builder<M> setModules(Set<M> modules) {
            Preconditions.checkNotNull(modules);
            Preconditions.checkArgument(!modules.isEmpty());
            this.modules = modules;
            return this;
        }

        @Override
        protected Builder<M> getThis() {
            return this;
        }

        /**
         * Builds and returns the {@link ProvidedModuleHolder}
         *
         * @return The {@link ProvidedModuleHolder}
         * @throws Exception Thrown if the builder how not got all the required information to run.
         */
        @Override
        public ProvidedModuleHolder<M> build() throws Exception {
            Preconditions.checkNotNull(modules);
            Preconditions.checkState(!modules.isEmpty());

            checkBuild();

            return new ProvidedModuleHolder<>(this);
        }
    }
}
