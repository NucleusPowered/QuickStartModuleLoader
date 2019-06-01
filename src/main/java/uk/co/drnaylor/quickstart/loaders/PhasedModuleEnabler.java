/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.loaders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.util.ThrownBiConsumer;
import uk.co.drnaylor.quickstart.util.ThrownConsumer;

import java.util.Set;

public class PhasedModuleEnabler<M extends Module, D extends M> {

    private final ImmutableMap<String, ThrownBiConsumer<D, ModuleHolder<M, D>, QuickStartModuleLoaderException>> disablePhases;
    private final ImmutableMap<String, ThrownConsumer<ModuleHolder<M, D>, QuickStartModuleLoaderException>> enablePrePhases;
    private final ImmutableMap<String, ThrownBiConsumer<M, ModuleHolder<M, D>, QuickStartModuleLoaderException>> enablePhases;
    private final ImmutableSet<String> phases;
    private final ImmutableSet<String> dPhases;

    PhasedModuleEnabler(ModuleEnablerBuilder<M, D> builder) {
        this.disablePhases = ImmutableMap.copyOf(builder.disablePhase);
        this.enablePhases = ImmutableMap.copyOf(builder.enablePhases);
        this.enablePrePhases = ImmutableMap.copyOf(builder.enablePrePhases);
        this.phases = ImmutableSet.copyOf(builder.phases);
        this.dPhases = ImmutableSet.copyOf(builder.dPhases);
    }

    public Set<String> getEnablePhases() {
        return this.phases;
    }

    public Set<String> getDisablePhases() {
        return this.dPhases;
    }

    public final void startEnablePrePhase(String phase, ModuleHolder<M, D> moduleHolder) throws Exception {
        final String lcPhase = phase.toLowerCase();
        ThrownConsumer<ModuleHolder<M, D>, QuickStartModuleLoaderException> ta = this.enablePrePhases.get(lcPhase);
        if (ta != null) {
            ta.apply(moduleHolder);
        }
    }

    public final void startEnablePhase(String phase, ModuleHolder<M, D> moduleHolder, M module) throws Exception {
        final String lcPhase = phase.toLowerCase();

        ThrownBiConsumer<M, ModuleHolder<M, D>, QuickStartModuleLoaderException> tc = this.enablePhases.get(lcPhase);
        if (tc != null) {
            tc.apply(module, moduleHolder);
        }
    }

    public final void startDisablePhase(String phase, ModuleHolder<M, D> moduleHolder, D module) throws Exception {
        final String lcPhase = phase.toLowerCase();

        ThrownBiConsumer<D, ModuleHolder<M, D>, QuickStartModuleLoaderException> tc = this.disablePhases.get(lcPhase);
        if (tc != null) {
            tc.apply(module, moduleHolder);
        }
    }

}
