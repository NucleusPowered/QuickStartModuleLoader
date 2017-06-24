/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.util;

@FunctionalInterface
public interface ThrownBiFunction<A, B, R, X extends Throwable> {

    /**
     * Returns a result, R, based on two input parameters. Can raise an error.
     *
     * @param a The first parameter
     * @param b The second parameter
     * @return The result
     * @throws X thrown if an exception of type X occurs.
     */
    R apply(A a, B b) throws X;

}
