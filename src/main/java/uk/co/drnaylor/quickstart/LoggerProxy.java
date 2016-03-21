/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

/**
 * An interface that allows logging to occur with other frameworks.
 */
public interface LoggerProxy {

    void info(String message);

    void warn(String message);

    void error(String message);
}
