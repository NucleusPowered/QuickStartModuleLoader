/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.enums;

import uk.co.drnaylor.quickstart.Module;

/**
 * Enumeration that describes the module loading phases.
 */
public enum ConstructionPhase {

    /**
     * The {@link uk.co.drnaylor.quickstart.ModuleContainer} has been constructed.
     */
    INITALISED,

    /**
     * Scanning the requested classpaths for {@link Module} classes.
     */
    DISCOVERING,

    /**
     * Modules have been discovered, but nothing has been constructed or enabled at this point.
     * Systems are not expecting to be able to interact with the modules.
     */
    DISCOVERED,

    /**
     * Modules are now being enabled. This means that the functionality is being activated for use in the system.
     */
    ENABLING,

    /**
     * Modules are now enabled. No more changes will be made to the modules.
     */
    ENABLED,

    /**
     * QuickStart Module Loader encountered an unrecoverable error and will not continue loading.
     */
    ERRORED;
}
