/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.holders.discoverystrategies;

import com.google.common.reflect.ClassPath;

import java.util.Set;
import java.util.stream.Collectors;

public class GoogleStrategy implements Strategy {

    @Override
    public Set<Class<?>> discover(String topPackage, ClassLoader classLoader) throws Exception {
        Set<ClassPath.ClassInfo> ci = ClassPath.from(classLoader).getTopLevelClassesRecursive(topPackage);
        return ci.stream().map(ClassPath.ClassInfo::load).collect(Collectors.toSet());
    }
}
