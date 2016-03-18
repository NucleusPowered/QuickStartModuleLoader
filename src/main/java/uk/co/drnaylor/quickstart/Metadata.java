/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import java.util.Optional;

public class Metadata {

    public final static String NAME = "@name@";
    public final static String VERSION = "@version@";

    private static boolean hasStarted = false;

    public static Optional<String> getStartupMessage() {
        if (!hasStarted) {
            hasStarted = true;
            return Optional.of("Starting " + NAME + " version " + VERSION + " subsystem.");
        }

        return Optional.empty();
    }
}
