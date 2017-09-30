/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.exceptions;

/**
 * Indicates an error during initialisation of QuickStart, or discovery of modules.
 */
public class QuickStartModuleDiscoveryException extends Exception {

    public QuickStartModuleDiscoveryException(String message) {
        super(message);
    }

    public QuickStartModuleDiscoveryException(String message, Exception innerException) {
        super(message, innerException);
    }
}
