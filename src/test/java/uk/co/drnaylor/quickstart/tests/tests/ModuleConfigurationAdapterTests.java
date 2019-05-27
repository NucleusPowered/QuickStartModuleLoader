/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.drnaylor.quickstart.DefaultLogger;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.SystemConfig;
import uk.co.drnaylor.quickstart.config.ModulesConfigAdapter;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ModuleConfigurationAdapterTests extends FakeLoaderTests {

    private SystemConfig<ConfigurationNode, Module> config;

    @Before
    @Override
    @SuppressWarnings("unchecked")
    public void beforeTests() throws Exception {
        super.beforeTests();

        Constructor<?> ctor = SystemConfig.class.getDeclaredConstructors()[0];
        ctor.setAccessible(true);
        config = (SystemConfig<ConfigurationNode, Module>) ctor.newInstance(
                loader,
                DefaultLogger.INSTANCE,
                (Function<ConfigurationOptions, ConfigurationOptions>) configurationOptions -> configurationOptions);

    }

    @Test
    public void testGettingModulesConfig() throws Exception {
        HashMap<String, LoadingStatus> m = Maps.newHashMap();
        m.put("d", LoadingStatus.DISABLED);
        m.put("e", LoadingStatus.ENABLED);
        m.put("f", LoadingStatus.FORCELOAD);
        HashMap<String, String> r = Maps.newHashMap();
        config.attachConfigAdapter(ModulesConfigAdapter.modulesKey, new ModulesConfigAdapter(m, r, DefaultLogger.INSTANCE, "modules", null));

        ModulesConfigAdapter mca = config.getConfigAdapterForModule("modules", ModulesConfigAdapter.class);
        Map<String, LoadingStatus> mm = mca.getNode();

        Assert.assertEquals(LoadingStatus.DISABLED, mm.get("d"));
        Assert.assertEquals(LoadingStatus.ENABLED, mm.get("e"));
        Assert.assertEquals(LoadingStatus.FORCELOAD, mm.get("f"));
    }

    @Test
    public void testModulesConfigSectionCanBeRelocated() throws Exception {
        HashMap<String, LoadingStatus> m = Maps.newHashMap();
        m.put("d", LoadingStatus.DISABLED);
        m.put("e", LoadingStatus.ENABLED);
        m.put("f", LoadingStatus.FORCELOAD);
        HashMap<String, String> r = Maps.newHashMap();
        config.attachConfigAdapter("m", new ModulesConfigAdapter(m, r, DefaultLogger.INSTANCE, "m", null));

        ModulesConfigAdapter mca = config.getConfigAdapterForModule("m", ModulesConfigAdapter.class);
        Map<String, LoadingStatus> mm = mca.getNode();

        Assert.assertEquals(LoadingStatus.DISABLED, mm.get("d"));
        Assert.assertEquals(LoadingStatus.ENABLED, mm.get("e"));
        Assert.assertEquals(LoadingStatus.FORCELOAD, mm.get("f"));
    }
}
