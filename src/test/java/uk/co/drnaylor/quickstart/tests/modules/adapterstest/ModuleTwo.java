package uk.co.drnaylor.quickstart.tests.modules.adapterstest;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.tests.config.adapters.SimpleWithDefault;

import java.util.Optional;

@ModuleData(id = "moduletwo", name = "moduletwo")
public class ModuleTwo implements Module {

    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return  Optional.of(new SimpleWithDefault());
    }

    @Override
    public void onEnable() {

    }
}
