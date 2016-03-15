package uk.co.drnaylor.quickstart.exceptions;

/**
 * Indicates an error during initialisation of QuickStart, or discovery of modules.
 */
public class QuickStartModuleDiscoveryException extends Exception {

    public QuickStartModuleDiscoveryException(String message, Exception innerException) {
        super(message, innerException);
    }
}
