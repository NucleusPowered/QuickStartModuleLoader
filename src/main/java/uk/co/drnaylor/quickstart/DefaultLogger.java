/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import java.util.logging.Logger;

public class DefaultLogger implements LoggerProxy {

    public final static DefaultLogger INSTANCE = new DefaultLogger();

    private DefaultLogger() {}

    @Override
    public void info(String message) {
        Logger.getLogger("QuickStart").info(message);
    }

    @Override
    public void warn(String message) {
        Logger.getLogger("QuickStart").warning(message);
    }

    @Override
    public void error(String message) {
        Logger.getLogger("QuickStart").severe(message);
    }
}
