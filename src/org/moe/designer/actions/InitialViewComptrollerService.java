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

package org.moe.designer.actions;

import org.moe.designer.ixml.IXmlFile;
import org.moe.designer.propertyTable.UniqueViewComponentProperty;
import org.moe.designer.utils.IOSPsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InitialViewComptrollerService {
    private final Project _project;
    private Map<String, Set<VirtualFile>> controlledAttributes;

    @NotNull
    public static InitialViewComptrollerService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, InitialViewComptrollerService.class);
    }

    public InitialViewComptrollerService(Project project) {
        _project = project;
        controlledAttributes = new ConcurrentHashMap<String, Set<VirtualFile>>(16, 0.75f, 1);
    }

    public void add(VirtualFile file){
        String parentFolder = file.getParent().getPath();
        if(controlledAttributes.containsKey(parentFolder)){
            controlledAttributes.get(parentFolder).add(file);
        }
        else{
            Set<VirtualFile> tmp = new HashSet<VirtualFile>();
            tmp.add(file);
            controlledAttributes.put(parentFolder, tmp);
        }

    }

    public void updateFolfderValues(final VirtualFile file, UniqueViewComponentProperty uniqueViewComponentProperty, Object value) throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                if(file != null) {
                    Set<VirtualFile> uniqueProps = controlledAttributes.get(file.getParent().getPath());
                    List<VirtualFile> deletedFiles = new ArrayList<VirtualFile>();
                    if (uniqueProps != null) {
                        for (VirtualFile vFile : uniqueProps) {
                            if (!vFile.getPath().equals(file.getPath())) {
                                IXmlFile psiFile = (IXmlFile) IOSPsiUtils.getPsiFileSafely(_project, vFile);
                                if(psiFile != null){
                                    XmlTag rootTag = psiFile.getRootTag();
                                    if (rootTag != null) {
                                        XmlAttribute attribute = rootTag.getAttribute("xrt:initialViewController");
                                        if (attribute != null) {
                                            rootTag.setAttribute("xrt:initialViewController", null);
                                        }
                                    }
                                }
                                else{
                                    deletedFiles.add(vFile);
                                }
                            }
                        }
                        uniqueProps.removeAll(deletedFiles);
                    }
                }
            }
        });
    }
}
