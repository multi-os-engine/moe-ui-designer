/*
Copyright 2014-2016 Intel Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.moe.designer;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UIDesignerPlugin {

    public static final String RELATIVE_PATH_FROM_MODULE_TO_XCODE_PRJ = "build" + File.separator + "xcode";

    public static Collection<Module> getMoeModules(Project project) {
        List<Module> modules = new ArrayList<Module>();

        for (Module module : ModuleManager.getInstance(project).getModules()) {

            if (isValidMoeModule(module)) {
                modules.add(module);
            }
        }

        return modules;
    }

    public static boolean isValidMoeModule(Module module) {
        if (module == null) {
            return false;
        }

        ModuleWithDependenciesScope libraries = (ModuleWithDependenciesScope) module.getModuleWithLibrariesScope();
        Collection<VirtualFile> roots = libraries.getRoots();

        boolean coreFound = false;
        boolean iosFound = false;
        for (VirtualFile vf : roots) {
            String name = vf.getName();
            coreFound = coreFound ? coreFound : name.equals("moe-core.jar");
            iosFound = iosFound ? iosFound : name.equals("moe-ios.jar");
            if (coreFound & iosFound) {
                return true;
            }
        }

        return false;
    }

    public static File getXcodeProjectFile(Module module) {
        if (module == null) {
            return null;
        }

        // Module file may not exist in the file system at this moment.
        String moduleDirPath = new File(module.getModuleFilePath()).getParent();

        if ((moduleDirPath == null) || moduleDirPath.isEmpty()) {
            return null;
        }

        String projectName = getXcodeProductName(moduleDirPath);

        if (projectName == null || projectName.isEmpty()) {
            return null;
        }

        String projectPath = moduleDirPath + File.separator + RELATIVE_PATH_FROM_MODULE_TO_XCODE_PRJ + File.separator + projectName + ".xcodeproj";

        return new File(projectPath);
    }

    public static String getXcodeProductName(String modulePath) {
        File file = new File(modulePath, RELATIVE_PATH_FROM_MODULE_TO_XCODE_PRJ);

        if (!file.exists() || !file.isDirectory()) {
            return null;
        }

        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".xcodeproj");
            }
        });

        if (files == null || files.length == 0) {
            return null;
        }

        String productName = files[0].getName();

        productName = productName.substring(0, productName.indexOf(".xcodeproj"));

        return productName;
    }

    public static Module findModuleForFile(Project project, VirtualFile file) {

        if (project == null || project.getBaseDir() == null) {
            return null;
        }

        String projectPath = project.getBaseDir().getPath();

        Module module = null;

        while (file != null && file.getPath().compareToIgnoreCase(projectPath) != 0) {
            module = ModuleUtil.findModuleForFile(file, project);

            if (module != null) {
                break;
            }

            file = file.getParent();
        }

        return module;
    }

}
