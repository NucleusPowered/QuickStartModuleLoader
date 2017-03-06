/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;
import uk.co.drnaylor.quickstart.enums.ModulePhase;

import java.util.Arrays;
import java.util.List;

/**
 * Internal specification of a module.
 */
public final class ModuleSpec {

    private final Class<? extends Module> moduleClass;
    private final List<String> softDeps;
    private final List<String> deps;
    private final String name;
    private final String id;
    private final boolean runtimeDisableable;
    private LoadingStatus status;
    private final boolean isMandatory;
    private ModulePhase phase = ModulePhase.DISCOVERED;

    ModuleSpec(Class<? extends Module> moduleClass, ModuleData data) {
        this(moduleClass, data.id(), data.name(), data.status(), data.isRequired(), Arrays.asList(data.softDependencies()), Arrays.asList(data.dependencies()));
    }

    ModuleSpec(Class<? extends Module> moduleClass, String id, String name, LoadingStatus status, boolean isMandatory) {
        this(moduleClass, id, name, status, isMandatory, Lists.newArrayList(), Lists.newArrayList());
    }

    ModuleSpec(Class<? extends Module> moduleClass, String id, String name, LoadingStatus status, boolean isMandatory, List<String> softDeps, List<String> deps) {
        Preconditions.checkNotNull(moduleClass);
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(status);
        Preconditions.checkNotNull(deps);
        Preconditions.checkNotNull(softDeps);

        this.id = id;
        this.moduleClass = moduleClass;
        this.runtimeDisableable = Module.RuntimeDisableable.class.isAssignableFrom(moduleClass);
        this.name = name;
        this.status = isMandatory ? LoadingStatus.FORCELOAD : status;
        this.isMandatory = isMandatory;
        this.softDeps = softDeps;
        this.deps = deps;
    }

    /**
     * Gets the {@link Class} that represents the {@link Module}
     *
     * @return The {@link Class}
     */
    public Class<? extends Module> getModuleClass() {
        return moduleClass;
    }

    /**
     * Gets the internal ID of the module.
     *
     * @return The ID of the module.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the human friendly name of the module.
     *
     * @return The name of the module.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the {@link LoadingStatus} for the module
     *
     * @return The {@link LoadingStatus}
     */
    public LoadingStatus getStatus() {
        return status;
    }

    /**
     * Sets the {@link LoadingStatus} for the module.
     *
     * @param status The {@link LoadingStatus}
     */
    void setStatus(LoadingStatus status) {
        Preconditions.checkState(phase == ModulePhase.DISCOVERED);
        Preconditions.checkState(!isMandatory);
        this.status = status;
    }

    /**
     * Gets whether the module is mandatory. This is equivalent to {@link LoadingStatus#FORCELOAD}, but it cannot be
     * changed via a config file.
     *
     * @return <code>true</code> if the module cannot be turned off.
     */
    public boolean isMandatory() {
        return isMandatory;
    }

    /**
     * Gets the current {@link ModulePhase} of the module.
     *
     * @return The phase of the module.
     */
    public ModulePhase getPhase() {
        return phase;
    }

    /**
     * Sets the phase of the module.
     *
     * @param phase The {@link ModulePhase}
     */
    void setPhase(ModulePhase phase) {
        this.phase = phase;
    }

    /**
     * Gets modules that should load before this one, if they are to be enabled.
     *
     * @return The list of the dependencies' IDs.
     */
    public List<String> getSoftDependencies() {
        return softDeps;
    }

    /**
     * Gets modules that <strong>must</strong> be loaded before this one.
     *
     * @return The list of the dependencies' IDs.
     */
    public List<String> getDependencies() {
        return deps;
    }

    /**
     * Returns whether this module can be enabled/disabled at runtime.
     *
     * @return <code>true</code> if so.
     */
    public boolean isRuntimeAlterable() {
        return runtimeDisableable;
    }
}
