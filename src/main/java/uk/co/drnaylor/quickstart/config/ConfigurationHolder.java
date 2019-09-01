/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.ModuleMetadata;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * Contains the configuration nodes and such logic
 */
public class ConfigurationHolder {

    private final static TypeToken<Map<String, LoadingStatus>> MODULE_SECTION_TYPE_TOKEN = new TypeToken<Map<String, LoadingStatus>>(){};

    private final Map<String, LoadingStatus> moduleDefaults = new HashMap<>();
    private final Map<String, LoadingStatus> modules = new HashMap<>();
    private final ObjectMapperFactory factory;

    private final Map<Class<? extends Module>, ConfigMetadata<?>> moduleConfigs = new HashMap<>();
    private final Map<Class<?>, ConfigMetadata<?>> objectToMetadata = new HashMap<>();

    private final ConfigurationLoader<? extends ConfigurationNode> loader;
    private final String moduleSectionHeader;
    private final String moduleSectionKey;
    private final Function<String, String> moduleDescriptionGenerator;
    private ConfigurationNode node;

    public ConfigurationHolder(
            ConfigurationLoader<? extends ConfigurationNode> loader,
            ObjectMapperFactory factory,
            String moduleSectionKey,
            String moduleSectionHeader,
            Function<String, String> moduleDescriptionGenerator) {
        this.loader = loader;
        this.moduleSectionKey = moduleSectionKey;
        this.moduleSectionHeader = moduleSectionHeader;
        this.moduleDescriptionGenerator = moduleDescriptionGenerator;
        this.factory = factory;
    }

    public <M extends Module> void registerModuleDefaults(Collection<ModuleMetadata<? extends M>> discoveredModules) {
        Preconditions.checkState(this.moduleDefaults.isEmpty(), "Already populated defaults");
        discoveredModules.forEach((metadata) -> this.moduleDefaults.put(metadata.getId(), metadata.getStatus()));
    }

    @SuppressWarnings("unchecked")
    public <T> void add(Class<? extends Module> module, ModuleMetadata<? extends Module> moduleMetadata, String header) throws ObjectMappingException {
        Preconditions.checkArgument(module.isAnnotationPresent(Configuration.class), "Must be a @Configuration module");
        Configuration annotation = module.getAnnotation(Configuration.class);
        Class<T> value = (Class<T>) annotation.value();
        ConfigTransformer transformer = null;
        if (annotation.transformer() != ConfigTransformer.class) {
            try {
                transformer = annotation.transformer().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        ConfigMetadata<T> metadata =
                new ConfigMetadata<>(header,
                        annotation.mergeIfPresent(),
                        this.factory.getMapper(value).bindToNew(),
                        value,
                        moduleMetadata.getId(),
                        moduleMetadata,
                        transformer);
        this.moduleConfigs.put(module, metadata);
        this.objectToMetadata.put(annotation.value(), metadata);
    }

    public <T> void remove(Class<? extends Module> module) {
        Preconditions.checkArgument(module.isAnnotationPresent(Configuration.class), "Must be a @Configuration module");
        Configuration annotation = module.getAnnotation(Configuration.class);
        this.moduleConfigs.remove(module);
        this.objectToMetadata.remove(annotation.value());
    }

    public Map<String, LoadingStatus> getModuleLoadingStatus() {
        return ImmutableMap.copyOf(this.modules);
    }

    public void load(boolean all) throws IOException, ObjectMappingException {
        this.node = this.loader.load();
        loadModuleSection();
        if (all) {
            loadModuleConfigs();
        }
    }

    public void loadModuleSection() throws ObjectMappingException {
        // load in the module.
        final ConfigurationNode moduleKeys = this.node.getNode(this.moduleSectionKey);
        if (moduleKeys instanceof CommentedConfigurationNode) {
            ((CommentedConfigurationNode) moduleKeys).setComment(this.moduleSectionHeader);
        }

        Map<String, LoadingStatus> modules = moduleKeys.getValue(MODULE_SECTION_TYPE_TOKEN, ImmutableMap.of());
        this.modules.clear();
        modules.forEach(this.modules::put);
        this.moduleDefaults.forEach((key, value) -> {
            ConfigurationNode m = moduleKeys.getNode(key);
            m.setValue(value);
            if (m instanceof CommentedConfigurationNode) {
                ((CommentedConfigurationNode) m).setComment(this.moduleDescriptionGenerator.apply(key));
            }
        });
        this.modules.forEach((key, status) -> {
            ConfigurationNode m = moduleKeys.getNode(key);
            m.setValue(status);
        });
    }

    public void loadModuleConfigs() throws ObjectMappingException {
        // Now, load each modules' config in turn
        for (Map.Entry<Class<? extends Module>, ConfigMetadata<?>> entry : this.moduleConfigs.entrySet()) {
            loadConfigSection(entry.getValue());
        }
    }

    public void saveBack() throws IOException {
        // Save the node back
        this.loader.save(this.node);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getConfigOptional(Class<T> configClass) {
        return Optional.ofNullable((ConfigMetadata<T>) this.objectToMetadata.get(configClass)).map(x -> x.instance.getInstance());
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfig(Class<T> configClass) {
        ConfigMetadata<T> metadata = (ConfigMetadata<T>) this.objectToMetadata.get(configClass);
        if (metadata == null) {
            throw new IllegalArgumentException();
        }

        return metadata.instance.getInstance();
    }

    private <T> void loadConfigSection(ConfigMetadata<T> entry) throws ObjectMappingException {
        // get the defaults
        ConfigurationNode n = this.node.getNode(entry.sectionName);
        boolean isNew = n.isVirtual();
        if (!isNew && entry.transformer != null) {
            // Transform
            entry.transformer.transformations().forEach(x -> x.apply(n));
        }
        if (isNew || entry.mergeIfPresent) {
            T def = this.factory.getMapper(entry.classType).bindToNew().getInstance();
            ConfigurationNode defNode = this.loader.createEmptyNode();
            defNode.setValue(TypeToken.of(entry.classType), def);
            this.node.getNode(entry.sectionName).mergeValuesFrom(defNode);
        }

        if (n instanceof CommentedConfigurationNode) {
            String comment = entry.header;
            if (comment != null) {
                ((CommentedConfigurationNode) n).setComment(comment);
            }
        }
        entry.instance.populate(this.node.getNode(entry.sectionName));
    }

    static class ConfigMetadata<T> {

        final String header;
        final boolean mergeIfPresent;
        final Class<T> classType;
        final ObjectMapper<T>.BoundInstance instance;
        final String sectionName;
        final ModuleMetadata<? extends Module> moduleMetadata;
        @Nullable final ConfigTransformer transformer;

        ConfigMetadata(String header, boolean mergeIfPresent,
                ObjectMapper<T>.BoundInstance instance,
                Class<T> classType,
                String sectionName,
                ModuleMetadata<? extends Module> moduleMetadata,
                @Nullable ConfigTransformer transformer) {
            this.header = header;
            this.mergeIfPresent = mergeIfPresent;
            this.instance = instance;
            this.classType = classType;
            this.sectionName = sectionName;
            this.moduleMetadata = moduleMetadata;
            this.transformer = transformer;
        }

    }

}
