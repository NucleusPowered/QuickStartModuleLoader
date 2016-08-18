/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import java.util.Optional;

class Metadata {

    private final static String NAME = "@name@";
    private final static String VERSION = "@version@";

    private static boolean hasStarted = false;

    static Optional<String> getStartupMessage() {
        if (!hasStarted) {
            hasStarted = true;
            return Optional.of("Starting " + NAME + " version " + VERSION + " subsystem.");
        }

        return Optional.empty();
    }
}
