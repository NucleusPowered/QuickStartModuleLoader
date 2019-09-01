/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.util;

@FunctionalInterface
public interface ObjectFactory {

    <T> T create(Class<T> object) throws Exception;

}
