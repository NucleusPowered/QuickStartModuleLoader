package uk.co.drnaylor.quickstart;

import com.google.common.base.Preconditions;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;
import uk.co.drnaylor.quickstart.enums.ModulePhase;

/**
 * Internal specification of a module.
 */
final class ModuleSpec {

    private final Class<? extends Module> moduleClass;
    private final String name;
    private final LoadingStatus status;
    private final boolean isMandatory;
    private ModulePhase phase = ModulePhase.DISCOVERED;

    ModuleSpec(Class<? extends Module> moduleClass, ModuleData data) {
        this(moduleClass, data.name(), data.status(), data.isRequired());
    }

    ModuleSpec(Class<? extends Module> moduleClass, String name, LoadingStatus status, boolean isMandatory) {
        Preconditions.checkNotNull(moduleClass);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(status);

        this.moduleClass = moduleClass;
        this.name = name;
        this.status = status;
        this.isMandatory = isMandatory;
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
     * Sets the current {@link ModulePhase} of the module.
     *
     * @param phase The current phase of the module.
     */
    public void setPhase(ModulePhase phase) {
        this.phase = phase;
    }
}
