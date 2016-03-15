/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.enums;

public enum ModulePhase {

    /**
     * The module has been discovered, but not constructed.
     */
    DISCOVERED,

    /**
     * The module has been constructed, but not enabled.
     */
    CONSTRUCTED,

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
