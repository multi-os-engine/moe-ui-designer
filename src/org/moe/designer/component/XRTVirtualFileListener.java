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

import org.moe.designer.actions.SegueDestinationController;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class XRTVirtualFileListener extends VirtualFileAdapter {
    private Project myProject;

    public XRTVirtualFileListener(Project myProject) {
        this.myProject = myProject;
    }

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent event){
        if(event != null && event.getFile() != null && event.getFile().getExtension() != null && event.getFile().getExtension().equals("ixml")){
            if(event.getPropertyName().equals("name")){
                SegueDestinationController controller = SegueDestinationController.getInstance(myProject);

                String newFileName = event.getParent().getPath() + File.separator + event.getNewValue();
                String oldFileName = event.getOldValue().toString();
                controller.replceDestination(newFileName, oldFileName);
            }
        }
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event){
        if(event != null && event.getFile() != null && event.getFile().getExtension() != null && event.getFile().getExtension().equals("ixml") && event.getRequestor() == null){
            SegueDestinationController controller = SegueDestinationController.getInstance(myProject);
            controller.addDestination(myProject, event.getFile());
            controller.restoreDestination();
        }
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event){
        if(event != null && event.getFile() != null && event.getFile().getExtension() != null && event.getFile().getExtension().equals("ixml")){
            SegueDestinationController controller = SegueDestinationController.getInstance(myProject);
            controller.removeDestination(event.getFile());
        }
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent event) {
        if(event != null && event.getFile() != null && event.getFile().getExtension() != null && event.getFile().getExtension().equals("ixml")){
            SegueDestinationController controller = SegueDestinationController.getInstance(myProject);
            controller.addDestination(myProject, event.getFile());
            controller.restoreDestination();
        }
    }

}
