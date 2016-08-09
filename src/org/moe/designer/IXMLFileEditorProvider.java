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


import com.android.resources.ResourceFolderType;
import org.moe.designer.actions.InitialViewComptrollerService;
import org.moe.designer.actions.SegueDestinationController;
import org.moe.designer.ixml.IXmlFile;
import org.moe.designer.rendering.ResourceHelper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IXMLFileEditorProvider implements FileEditorProvider, DumbAware {

    public static final String ANDROID_DESIGNER_ID = "ios-UI-designer";

    public static boolean acceptLayout(final @NotNull Project project, final @NotNull VirtualFile file) {
        PsiFile psiFile = getPsiFileSafely(project, file);
        ResourceFolderType folderType = ResourceHelper.getFolderType(psiFile);

        Module foundModule = UIDesignerPlugin.findModuleForFile(project, file);
        boolean isMoeModule = UIDesignerPlugin.isValidMoeModule(foundModule);

        return (psiFile instanceof IXmlFile) &&
                (folderType != null && folderType.equals(ResourceFolderType.LAYOUT)) &&
                isMoeModule;
    }

    @Nullable
    public static PsiFile getPsiFileSafely(@NotNull final Project project, @NotNull final VirtualFile file) {
        return ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>() {
            @Nullable
            @Override
            public PsiFile compute() {
                return file.isValid() ? PsiManager.getInstance(project).findFile(file) : null;
            }
        });
    }

    @Override
    public boolean accept(Project project, VirtualFile virtualFile) {
        return acceptLayout(project, virtualFile);
    }

    @NotNull
    @Override
    public FileEditor createEditor(Project project, VirtualFile virtualFile) {
        SegueDestinationController.getInstance(project).addDestination(project, virtualFile);
        InitialViewComptrollerService.getInstance(project).add(virtualFile);
        return new IOSDesignerEditor(project, virtualFile);
    }

    @Override
    public void disposeEditor(@NotNull FileEditor fileEditor) {

    }

    @NotNull
    @Override
    public FileEditorState readState(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile virtualFile) {
        return FileEditorState.INSTANCE;
    }

    @Override
    public void writeState(@NotNull FileEditorState fileEditorState, @NotNull Project project, @NotNull Element element) {

    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return ANDROID_DESIGNER_ID;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
