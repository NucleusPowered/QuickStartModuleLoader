package uk.co.drnaylor.quickstart.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import uk.co.drnaylor.quickstart.LoggerProxy;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains the configuration nodes and such logic
 */
public class ConfigurationHolder<N extends ConfigurationNode> {

    private final static TypeToken<Map<String, LoadingStatus>> MODULE_SECTION_TYPE_TOKEN = new TypeToken<Map<String, LoadingStatus>>(){};

    private final Map<String, LoadingStatus> moduleDefaults;
    private final Map<String, LoadingStatus> modules = new HashMap<>();
    private final ObjectMapperFactory factory;

    private final Map<Class<? extends Module>, ConfigMetadata<?>> moduleConfigs = new HashMap<>();
    private final Map<Class<?>, ConfigMetadata<?>> objectToMetadata = new HashMap<>();

    private final ConfigurationLoader<N> loader;
    private final String moduleSectionHeader;
    private N node;
    private final LoggerProxy loggerProxy;

    public ConfigurationHolder(
            ConfigurationLoader<N> loader,
            LoggerProxy proxy,
            ObjectMapperFactory factory,
            String moduleSectionHeader,
            Map<String, LoadingStatus> moduleDefaults) {
        this.loader = loader;
        this.loggerProxy = proxy;
        this.moduleDefaults = moduleDefaults;
        this.moduleSectionHeader = moduleSectionHeader;
        this.factory = factory;
    }

    public <T> void add(Class<? extends Module> module, String moduleSection, String header) throws ObjectMappingException {
        Preconditions.checkArgument(module.isAnnotationPresent(Configuration.class), "Must be a @Configuration module");
        Configuration annotation = module.getAnnotation(Configuration.class);
        Class<T> value = (Class<T>) annotation.value();
        ConfigMetadata<T> metadata =
                new ConfigMetadata<T>(header,
                        annotation.mergeIfPresent(),
                        this.factory.getMapper(value).bindToNew(),
                        value,
                        moduleSection);
        this.moduleConfigs.put(module, metadata);
        this.objectToMetadata.put(annotation.value(), metadata);
    }

    public void load() throws IOException, ObjectMappingException {
        this.node = this.loader.load();

        // load in the Module Section Header.
        Map<String, LoadingStatus> modules = this.node.getNode(this.moduleSectionHeader).getValue(MODULE_SECTION_TYPE_TOKEN, ImmutableMap.of());
        this.modules.clear();
        modules.forEach(this.modules::put);
        this.moduleDefaults.forEach(this.modules::putIfAbsent);

        // Now, load each in turn
        for (Map.Entry<Class<? extends Module>, ConfigMetadata<?>> entry : this.moduleConfigs.entrySet()) {
            loadConfigSection(entry.getValue());
        }

        // Save the node back
        this.loader.save(this.node);
    }

    private <T> void loadConfigSection(ConfigMetadata<T> entry) throws ObjectMappingException {
        // get the defaults
        boolean isNew = this.node.getNode(entry.sectionName).isVirtual();
        if (isNew || entry.mergeIfPresent) {
            T def = this.factory.getMapper(entry.classType).bindToNew().getInstance();
            N defNode = this.loader.createEmptyNode();
            defNode.setValue(TypeToken.of(entry.classType), def);
            this.node.getNode(entry.sectionName).mergeValuesFrom(defNode);
        }

        entry.instance.populate(this.node.getNode(entry.sectionName));
    }

    static class ConfigMetadata<T> {

        final String header;
        final boolean mergeIfPresent;
        final Class<T> classType;
        final ObjectMapper<T>.BoundInstance instance;
        final String sectionName;

        ConfigMetadata(String header, boolean mergeIfPresent, ObjectMapper<T>.BoundInstance instance, Class<T> classType, String sectionName) {
            this.header = header;
            this.mergeIfPresent = mergeIfPresent;
            this.instance = instance;
            this.classType = classType;
            this.sectionName = sectionName;
        }

    }

}
