/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a module as having configuration attached
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {

    /**
     * The type of configuration object. Must be {@link ConfigSerializable} or
     * have an associated {@link TypeSerializer}
     *
     * @return The type of configuration object.
     */
    Class<?> value();

    /**
     * If set, then on config construction, a copy of this will be merged into the
     * configuration structure using {@link ConfigurationNode#mergeValuesFrom(ConfigurationNode)}.
     * Otherwise, the object will simply be populated from the node.
     *
     * <p>Note that this will predominately affect lists and maps.</p>
     *
     * @return true if so, false otherwise
     */
    boolean mergeIfPresent() default true;

    /**
     * Provides a class that will transform configuration sections. The class
     * MUST have a no-args constructor and instantiatable.
     *
     * @return The transformer. If the {@link ConfigTransformer}, indicates no transformations.
     */
    Class<? extends ConfigTransformer> transformer() default ConfigTransformer.class;

}
