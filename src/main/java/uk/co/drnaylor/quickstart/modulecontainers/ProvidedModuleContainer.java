/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.modulecontainers;

import com.google.common.base.Preconditions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import uk.co.drnaylor.quickstart.LoggerProxy;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.ModuleContainer;
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

public final class ProvidedModuleContainer extends ModuleContainer {

    public static Builder builder() {
        return new Builder();
    }

    private final Map<Class<? extends Module>, Module> moduleMap;

    /**
     * Constructs a {@link ModuleContainer} and starts discovery of the modules.
     *
     * @param configurationLoader The {@link ConfigurationLoader} that contains details of whether the modules should be enabled or not.
     * @param moduleEnabler       The {@link ModuleEnabler} that contains the logic to enable modules.
     * @param loggerProxy         The {@link LoggerProxy} that contains methods to send messages to the logger, or any other source.
     * @param onPreEnable         The {@link Procedure} to run on pre enable, before modules are pre-enabled.
     * @param onEnable            The {@link Procedure} to run on enable, before modules are pre-enabled.
     * @param onPostEnable        The {@link Procedure} to run on post enable, before modules are pre-enabled.
     * @param modules             The {@link Module}s to load.
     * @param function            The {@link Function} that converts {@link ConfigurationOptions}.
     * @param requireAnnotation   Whether modules require a {@link ModuleData} annotation.
     * @param processDoNotMerge   Whether module configs will have {@link NoMergeIfPresent} annotations processed.
     *
     * @throws QuickStartModuleDiscoveryException if there is an error starting the Module Container.
     */
    private <N extends ConfigurationNode> ProvidedModuleContainer(ConfigurationLoader<N> configurationLoader,
                                                                  LoggerProxy loggerProxy,
                                                                  ModuleEnabler moduleEnabler,
                                                                  Procedure onPreEnable,
                                                                  Procedure onEnable,
                                                                  Procedure onPostEnable,
                                                                  Set<Module> modules,
                                                                  Function<ConfigurationOptions, ConfigurationOptions> function,
                                                                  boolean requireAnnotation,
                                                                  boolean processDoNotMerge,
                                                                  @Nullable Function<Module, String> headerProcessor,
                                                                  @Nullable Function<Class<? extends Module>, String> descriptionProcessor)
            throws QuickStartModuleDiscoveryException {
        super(configurationLoader, loggerProxy, moduleEnabler, onPreEnable, onEnable, onPostEnable, function, requireAnnotation, processDoNotMerge,
                headerProcessor, descriptionProcessor);
        moduleMap = modules.stream().collect(Collectors.toMap(Module::getClass, v -> v));
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

    public static class Builder extends ModuleContainer.Builder<ProvidedModuleContainer, Builder> {

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
         * Builds and returns the {@link ProvidedModuleContainer}
         *
         * @return The {@link ProvidedModuleContainer}
         * @throws Exception Thrown if the builder how not got all the required information to run.
         */
        @Override
        public ProvidedModuleContainer build() throws Exception {
            Preconditions.checkNotNull(modules);
            Preconditions.checkState(!modules.isEmpty());

            checkBuild();

            return new ProvidedModuleContainer(configurationLoader, loggerProxy, enabler, onPreEnable, onEnable, onPostEnable,
                    modules, configurationOptionsTransformer, requireAnnotation, doNotMerge, moduleConfigurationHeader, moduleDescriptionHandler);
        }
    }
}
