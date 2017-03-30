package uk.co.drnaylor.quickstart.tests.config.serialisables;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import uk.co.drnaylor.quickstart.config.NoMergeIfPresent;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class MergeTest {

    @Setting("merge")
    private Map<String, String> merge = new HashMap<String, String>() {{
        put("a", "1");
    }};

    @NoMergeIfPresent
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
