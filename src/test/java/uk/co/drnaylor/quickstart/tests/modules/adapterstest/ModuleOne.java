package uk.co.drnaylor.quickstart.tests.modules.adapterstest;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.config.SimpleNodeConfigAdapter;

import java.util.Optional;

@ModuleData(id = "moduleone", name = "moduleone")
public class ModuleOne implements Module {

    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.of(new SimpleNodeConfigAdapter());
    }

    @Override
    public void onEnable() {

    }
}
