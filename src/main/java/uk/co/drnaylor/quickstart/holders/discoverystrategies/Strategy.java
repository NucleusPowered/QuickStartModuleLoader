/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.holders.discoverystrategies;

import java.util.Set;

/**
 * A {@link Strategy} determines how to discover a set of classes.
 */
@FunctionalInterface
public interface Strategy {

    /**
     * The default strategy for discovery
     */
    Strategy DEFAULT = new GoogleStrategy();

    /**
     * Discover classes accessible using the specified {@link ClassLoader} that are in
     * the package (or sub-package of) the supplied package name
     *
     * @param topPackage The top level package to scan
     * @param classLoader The {@link ClassLoader} to use
     * @return The {@link Set} of {@link Class}es that were discovered
     * @throws Exception thrown if some issue occurred
     */
    Set<Class<?>> discover(String topPackage, ClassLoader classLoader) throws Exception;

}
