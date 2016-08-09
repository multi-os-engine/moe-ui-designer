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

import org.moe.designer.utils.IOSPsiUtils;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.designer.DesignerEditor;
import com.intellij.designer.designSurface.DesignerEditorPanel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IOSDesignerEditor extends DesignerEditor {

    public IOSDesignerEditor(Project project, VirtualFile file) {
        super(project, file);
    }


    @Override
    protected Module findModule(Project project, VirtualFile file) {
        Module module = ModuleUtilCore.findModuleForFile(file, project);
        if (module == null) {
            module = IOSPsiUtils.getModuleSafely(project, file);
        }
        if (module == null) {
            throw new IllegalArgumentException("No module for file " + file + " in project " + project);
        }
        return module;
    }

    @NotNull
    @Override
    protected DesignerEditorPanel createDesignerPanel(Project project, Module module, VirtualFile file) {
        System.out.println("Create designer panel");
        return new IOSDesignerEditorPanel(this,project,module,file);
       // return null;
    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }
}
