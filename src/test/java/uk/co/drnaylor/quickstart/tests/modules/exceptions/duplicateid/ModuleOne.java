/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.tests.modules.exceptions.duplicateid;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "one", name = "one")
public class ModuleOne implements Module {

    @Override public void onEnable() {

    }
}
