/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.config.serialisables;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class MergeTest {

    @Setting("merge")
    private Map<String, String> merge = new HashMap<String, String>() {{
        put("a", "1");
    }};

    @Setting("nomerge")
    private Map<String, String> nomerge = new HashMap<String, String>() {{
        put("a", "1");
    }};

    public Map<String, String> getMerge() {
        return merge;
    }

    public Map<String, String> getNomerge() {
        return nomerge;
    }
}
