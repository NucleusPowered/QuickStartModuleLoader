/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.tests;

import com.google.common.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;
import uk.co.drnaylor.quickstart.ModuleContainer;
import uk.co.drnaylor.quickstart.tests.config.adapters.Typed;
import uk.co.drnaylor.quickstart.tests.modules.donotmergetest.MergeTestModule;
import uk.co.drnaylor.quickstart.tests.scaffolding.FakeLoaderTests;

import java.util.HashMap;
import java.util.Map;

public class NoMergeConfigTests extends FakeLoaderTests {

    private TypeToken<Map<String, String>> ttmss = new TypeToken<Map<String, String>>() {};

    @Test
    public void testThatMergingDoNotMergeWorksAsExpected() throws Exception {
        n.getNode("mergetest").getNode("merge").setValue(ttmss, new HashMap<String, String>() {{
            put("here", "1");
        }});

        n.getNode("mergetest").getNode("nomerge").setValue(ttmss, new HashMap<String, String>() {{
           put("here", "1");
       }});

        ModuleContainer container = getProvidedContainer(new MergeTestModule());
        container.loadModules(true);
        Assert.assertEquals(2, container.getConfigAdapterForModule("mergetest", Typed.class).getNode().getMerge().size());
        Assert.assertEquals(1, container.getConfigAdapterForModule("mergetest", Typed.class).getNode().getNomerge().size());
    }
}
