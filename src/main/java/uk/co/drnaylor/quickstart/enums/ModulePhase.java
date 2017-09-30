/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.enums;

import uk.co.drnaylor.quickstart.enums.interfaces.IModulePhase;

public enum ModulePhase implements IModulePhase {

    /**
     * The module has been discovered, but not constructed.
     */
    DISCOVERED {
        @Override public boolean canSetLoadingPhase() {
            return true;
        }
    },

    /**
     * The module has been constructed, but not enabled, and is going through dependency checks.
     */
    CONSTRUCTED {
        @Override public boolean canSetLoadingPhase() {
            return true;
        }
    },

    /**
     * The module has been enabled.
     */
    ENABLED,

    /**
     * The module will not be enabled.
     */
    DISABLED,

    /**
     * The module errored and is not enabled.
     */
    ERRORED
}
