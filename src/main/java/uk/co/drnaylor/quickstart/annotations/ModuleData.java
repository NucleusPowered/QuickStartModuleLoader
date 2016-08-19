/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.annotations;

import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.lang.annotation.*;

/**
 * This annotation is intended to be used on {@link uk.co.drnaylor.quickstart.Module} files. It specifies the default
 * status of the module using the {@link LoadingStatus} enum, and other metadata.
 *
 * <p>
 *     If a module is discovered without this annotation, then we'll try to make do...
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ModuleData {

    /**
     * The ID of the module.
     *
     * @return The ID of the module.
     */
    String id();

    /**
     * The human friendly name for this module.
     *
     * @return The name of this module.
     */
    String name();

    /**
     * Specifies the {@link uk.co.drnaylor.quickstart.Module}s that should be loaded before this one. Modules that are
     * disabled will not prevent this module from loading.
     *
     * @return An array of module IDs to load before.
     */
    String[] softDependencies() default {};

    /**
     * Specified s the {@link uk.co.drnaylor.quickstart.Module}s that <strong>must</strong> be loaded before this one.
     * Modules that are dependencies and are disabled <strong>will</strong> prevent this module from loading.
     *
     * @return An array of module IDs to load before.
     */
    String[] dependencies() default {};

    /**
     * The default {@link LoadingStatus} for this module, if nothing is defined.
     *
     * @return The {@link LoadingStatus}
     */
    LoadingStatus status() default LoadingStatus.ENABLED;

    /**
     * Marks the module as required.
     *
     * @return <code>true</code> if the module just can't be disabled.
     */
    boolean isRequired() default false;
}
