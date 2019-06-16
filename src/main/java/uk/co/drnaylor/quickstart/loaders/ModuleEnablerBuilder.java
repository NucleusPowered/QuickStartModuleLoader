/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.loaders;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.util.ThrownBiConsumer;
import uk.co.drnaylor.quickstart.util.ThrownConsumer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class ModuleEnablerBuilder<M extends Module, D extends M> {

    final LinkedHashMap<String, ThrownBiConsumer<D, ModuleHolder<M, D>, QuickStartModuleLoaderException>> disablePhase = new LinkedHashMap<>();
    final LinkedHashMap<String, ThrownConsumer<ModuleHolder<M, D>, QuickStartModuleLoaderException>> enablePrePhases = new LinkedHashMap<>();
    final LinkedHashMap<String, ThrownBiConsumer<M, ModuleHolder<M, D>, QuickStartModuleLoaderException>> enablePhases = new LinkedHashMap<>();
    final Set<String> phases = new LinkedHashSet<>();
    final Set<String> dPhases = new LinkedHashSet<>();

    public ModuleEnablerBuilder(Class<M> m, Class<D> d) {}

    public ModuleEnablerBuilder<M, D> createPreEnablePhase(String name,
            ThrownConsumer<ModuleHolder<M, D>, QuickStartModuleLoaderException> prePhaseActions) {
        String lcName = name.toLowerCase();
        this.phases.add(lcName);
        if (this.enablePrePhases.containsKey(lcName)) {
            throw new IllegalArgumentException("name has already been added");
        }

        this.enablePrePhases.put(name.toLowerCase(), prePhaseActions);
        return this;
    }

    public ModuleEnablerBuilder<M, D> createEnablePhase(String name,
            ThrownBiConsumer<M, ModuleHolder<M, D>, QuickStartModuleLoaderException> phaseActions) {
        String lcName = name.toLowerCase();
        this.phases.add(lcName);
        if (this.enablePhases.containsKey(lcName)) {
            throw new IllegalArgumentException("name has already been added");
        }

        this.enablePhases.put(name.toLowerCase(), phaseActions);
        return this;
    }

    public ModuleEnablerBuilder<M, D> createDisablePhase(String name,
            ThrownBiConsumer<D, ModuleHolder<M, D>, QuickStartModuleLoaderException> phaseActions) {
        String lcName = name.toLowerCase();
        this.dPhases.add(lcName);
        if (this.disablePhase.containsKey(lcName)) {
            throw new IllegalArgumentException("name has already been added");
        }

        this.disablePhase.put(name.toLowerCase(), phaseActions);
        return this;
    }

    public PhasedModuleEnabler<M, D> build() {
        return new PhasedModuleEnabler<>(this);
    }
}
