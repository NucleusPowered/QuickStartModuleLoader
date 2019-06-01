/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.util;

/**
 * A consumer that can throw an exception
 *
 * @param <A> The object to consume.
 * @param <X> The exception
 */
public interface ThrownConsumer<A, X extends Throwable> {

    /**
     * Consumes an object and operates on it
     *
     * @param in The object to consume
     * @throws X a checked exception this can throw
     */
    void apply(A in) throws X;

}
