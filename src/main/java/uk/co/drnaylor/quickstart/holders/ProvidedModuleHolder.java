/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.holders;

import com.google.common.base.Preconditions;
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
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.loaders.ModuleEnabler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * The provided module container is provided pre-constructed {@link Module}s to work with.
 */
public final class ProvidedModuleHolder extends ModuleHolder {

    public static Builder builder() {
        return new Builder();
    }

    private final Map<Class<? extends Module>, Module> moduleMap;

    private ProvidedModuleHolder(Builder builder) throws QuickStartModuleDiscoveryException {
        super(builder);
        this.moduleMap = builder.modules.stream().collect(Collectors.toMap(Module::getClass, v -> v));
    }

    @Override
    protected Set<Class<? extends Module>> discoverModules() throws Exception {
        // They've kind of been discovered.
        return moduleMap.keySet();
    }

    @Override
    protected Module getModule(ModuleSpec spec) throws Exception {
        Module module = moduleMap.get(spec.getModuleClass());
        if (module == null) {
            throw new NoModuleException(spec.getName());
        }

        return module;
    }

    public static class Builder extends ModuleHolder.Builder<ProvidedModuleHolder, Builder> {

        private Set<Module> modules;

        /**
         * Sets the {@link Set} of {@link Module}s that QSML should load.
         *
         * @param modules The set of moudules to load.
         * @return This {@link Builder}, for chaining.
         */
        public Builder setModules(Set<Module> modules) {
            Preconditions.checkNotNull(modules);
            Preconditions.checkArgument(!modules.isEmpty());
            this.modules = modules;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        /**
         * Builds and returns the {@link ProvidedModuleHolder}
         *
         * @return The {@link ProvidedModuleHolder}
         * @throws Exception Thrown if the builder how not got all the required information to run.
         */
        @Override
        public ProvidedModuleHolder build() throws Exception {
            Preconditions.checkNotNull(modules);
            Preconditions.checkState(!modules.isEmpty());

            checkBuild();

            return new ProvidedModuleHolder(this);
        }
    }
}
