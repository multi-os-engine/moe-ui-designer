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

package org.moe.designer.component;


import com.intellij.ProjectTopics;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.*;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

public class XRTProjectComponent extends AbstractProjectComponent {
    private MessageBusConnection messageBusConnection;

    protected XRTProjectComponent(Project project) {
        super(project);
    }

    @Override
    public void initComponent() {
        super.initComponent();
        messageBusConnection = myProject.getMessageBus().connect();
        messageBusConnection.subscribe(ProjectTopics.MODULES, new XRTModuleSyncListener());
        VirtualFileManager.getInstance().addVirtualFileListener(new XRTVirtualFileListener(myProject), myProject);
    }

    @Override
    public void disposeComponent() {
        messageBusConnection.disconnect();
        super.disposeComponent();
    }

    @Override
    public void projectOpened() {
    }
}
