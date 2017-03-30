/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.AtomicFiles;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.co.drnaylor.quickstart.config.AbstractAdaptableConfig;
import uk.co.drnaylor.quickstart.tests.config.SimpleConfig;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Function;

public class ConfigFileTests extends FakeLoaderTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Test that changing the ConfigurationOptions via the function argument between loads works.
     *
     * @throws Exception I hope not!
     */
    @Test
    public void testThatConfigurationOptionsFunctionWorksAsExpected() throws Exception {
        URL url = getClass().getResource("/test.json");
        final Path tempFile = folder.newFile().toPath();
        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream())))
                .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, UTF_8)).setLenient(true).build();

        // Create the SimpleConfig
        SerialiserTransformer st = new SerialiserTransformer();
        SimpleConfig simpleConfig = new SimpleConfig(loader, () -> loader.createEmptyNode(st.apply(loader.getDefaultOptions())), st);

        Field field = AbstractAdaptableConfig.class.getDeclaredField("node");
        field.setAccessible(true);

        // The transformer shouldn't add any serialisers.
        simpleConfig.load();
        Assert.assertNull(((ConfigurationNode)field.get(simpleConfig)).getOptions().getSerializers().get(TypeToken.of(Dummy.class)));

        st.set = true;

        // The transformer shouldn't have added any serialisers yet.
        Assert.assertNull(((ConfigurationNode)field.get(simpleConfig)).getOptions().getSerializers().get(TypeToken.of(Dummy.class)));

        // It will on next load.
        simpleConfig.load();
        Assert.assertEquals(DummySerialiser.class, ((ConfigurationNode)field.get(simpleConfig)).getOptions().getSerializers().get(TypeToken.of(Dummy.class)).getClass());
    }

    private class SerialiserTransformer implements Function<ConfigurationOptions, ConfigurationOptions> {

        private boolean set = false;

        @Override
        public ConfigurationOptions apply(ConfigurationOptions configurationOptions) {
            if (!set) {
                return configurationOptions;
            }

            TypeSerializerCollection tsc = configurationOptions.getSerializers();
            tsc.registerType(TypeToken.of(Dummy.class), new DummySerialiser());
            configurationOptions.setSerializers(tsc);
            return configurationOptions;
        }
    }

    private static class Dummy {}

    private static class DummySerialiser implements TypeSerializer<Dummy> {

        @Override
        public Dummy deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            return null;
        }

        @Override
        public void serialize(TypeToken<?> type, Dummy obj, ConfigurationNode value) throws ObjectMappingException {

        }
    }
}
