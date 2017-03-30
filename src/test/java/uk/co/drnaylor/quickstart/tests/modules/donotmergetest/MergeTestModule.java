package uk.co.drnaylor.quickstart.tests.modules.donotmergetest;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.tests.config.adapters.Typed;

import java.util.Optional;

@ModuleData(id = "mergetest", name = "mergetest")
public class MergeTestModule implements Module {

    @Override public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.of(new Typed());
    }

    @Override public void onEnable() {

    }
}
