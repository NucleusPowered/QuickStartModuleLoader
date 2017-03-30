package uk.co.drnaylor.quickstart.tests.config.adapters;

import uk.co.drnaylor.quickstart.config.TypedAbstractConfigAdapter;
import uk.co.drnaylor.quickstart.tests.config.serialisables.MergeTest;

public class Typed extends TypedAbstractConfigAdapter.StandardWithSimpleDefault<MergeTest> {

    public Typed() {
        super(MergeTest.class);
    }
}
