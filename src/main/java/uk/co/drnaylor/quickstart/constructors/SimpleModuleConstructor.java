/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.constructors;

import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;

/**
 * A simple module constructor that tries to construct using a no-args constructor via reflection, and enable it
 * by simply calling the {@link Module#onEnable()} method.
 */
public class SimpleModuleConstructor implements ModuleConstructor {

    /**
     * Gets the instance of this {@link SimpleModuleConstructor}
     */
    public static final SimpleModuleConstructor INSTANCE = new SimpleModuleConstructor();

    private SimpleModuleConstructor() { }

    @Override
    public Module constructModule(Class<? extends Module> moduleClass) throws QuickStartModuleLoaderException.Construction {
        try {
            return moduleClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new QuickStartModuleLoaderException.Construction(moduleClass, "Unable to construct the module: " + moduleClass.getName(), e);
        }
    }

    @Override
    public void enableModule(Module module) throws QuickStartModuleLoaderException.Enabling {
        try {
            module.onEnable();
        } catch (Exception e) {
            throw new QuickStartModuleLoaderException.Enabling(module.getClass(), "Unable to enable the module: " + module.getClass().getName(), e);
        }
    }
}
