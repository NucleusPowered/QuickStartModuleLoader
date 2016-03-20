/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.drnaylor.quickstart.SystemConfig;
import uk.co.drnaylor.quickstart.config.ModulesConfigAdapter;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class ModuleConfigurationAdapterTests extends FakeLoaderTests {

    private SystemConfig<ConfigurationNode, ConfigurationLoader<ConfigurationNode>> config;

    @Before
    @Override
    public void beforeTests() throws Exception {
        super.beforeTests();

        Constructor<?> ctor = SystemConfig.class.getDeclaredConstructors()[0];
        ctor.setAccessible(true);
        config = (SystemConfig<ConfigurationNode, ConfigurationLoader<ConfigurationNode>>) ctor.newInstance(loader);

        HashMap<String, LoadingStatus> m = Maps.newHashMap();
        m.put("d", LoadingStatus.DISABLED);
        m.put("e", LoadingStatus.ENABLED);
        m.put("f", LoadingStatus.FORCELOAD);
        config.attachConfigAdapter(ModulesConfigAdapter.modulesKey, new ModulesConfigAdapter(m));
    }

    @Test
    public void testGettingModulesConfig() throws NoModuleException, IncorrectAdapterTypeException, ObjectMappingException {
        ModulesConfigAdapter mca = config.getConfigAdapterForModule("modules", ModulesConfigAdapter.class);
        Map<String, LoadingStatus> m = mca.getNode();

        Assert.assertEquals(LoadingStatus.DISABLED, m.get("d"));
        Assert.assertEquals(LoadingStatus.ENABLED, m.get("e"));
        Assert.assertEquals(LoadingStatus.FORCELOAD, m.get("f"));
    }
}
