/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.config.adapters;

import uk.co.drnaylor.quickstart.tests.config.serialisables.MergeTest;

public class Typed extends TypedAbstractConfigAdapter.StandardWithSimpleDefault<MergeTest> {

    public Typed() {
        super(MergeTest.class);
    }
}
