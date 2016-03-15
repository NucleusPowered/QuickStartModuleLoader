/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.enums;

public enum LoadingStatus {

    /**
     * The default state of a module, will be loaded unless it has been requested that the module should be turned off.
     */
    ENABLED,

    /**
     * The module will not be loaded.
     */
    DISABLED,

    /**
     * The module will always be loaded, regardless of whether a request to turn the module off has been received.
     */
    FORCELOAD
}
