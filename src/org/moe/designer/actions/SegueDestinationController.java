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

import org.moe.designer.android.dom.resources.ResourceElement;
import org.moe.designer.android.dom.resources.Resources;
import org.moe.designer.ixml.IXmlFile;
import org.moe.designer.ixml.IXmlFileImpl;
import org.moe.designer.utils.IOSPsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.XmlTagUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class SegueDestinationController {
    private Set<CompositDestination> allFiles;
    private Set<CompositDestination> navigationController;
    private Project _project;

    @NotNull
    public static SegueDestinationController getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, SegueDestinationController.class);
    }

    public SegueDestinationController(Project project) {
        _project = project;
        allFiles = new HashSet<CompositDestination>();
        navigationController = new HashSet<CompositDestination>();
    }

    //add destinations
    public void addDestination(Project project, VirtualFile file){
        CompositDestination tmp = new CompositDestination(project, file);
        if(isNavigationController(tmp)){
            navigationController.add(tmp);
        }
        else{
            allFiles.add(tmp);
        }
    }

    public void addDestination(Project project, PsiFile file){
        CompositDestination tmp = new CompositDestination(project, file);
        if(isNavigationController(tmp)){
            navigationController.add(tmp);
        }
        else{
            allFiles.add(tmp);
        }
    }

    public void reInitDestination(){
        Set<CompositDestination> overalCollection = new HashSet<CompositDestination>(allFiles);
        overalCollection.addAll(navigationController);

        for (CompositDestination element : overalCollection){
            element.clearDestination();
        }
        restoreDestination();
    }

    public void restoreDestination(){
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                Set<CompositDestination> overalCollection = new HashSet<CompositDestination>(allFiles);
                overalCollection.addAll(navigationController);

                for (CompositDestination element : overalCollection) {
                    PsiElement[] tags = ((IXmlFileImpl) element.getFile()).getDocument().getChildren();
                    for (PsiElement tag : tags) {
                        if (tag instanceof XmlTag) {
                            recursiveRestoreRelationship(element, (XmlTag) tag);
                        }
                    }
                }

                removeAllNavigationBars();
                updateAllNavigationBars();
            }
        });

    }

    public void removeDestination(VirtualFile file){
        CompositDestination dest = findDestination(file.getPath());
        if(dest == null){
            dest = findController(file.getPath());
        }

        if(allFiles.contains(dest)){
            allFiles.remove(dest);
            Set<CompositDestination> overalCpllection = new HashSet<CompositDestination>(allFiles);
            overalCpllection.addAll(navigationController);

            for(CompositDestination element : overalCpllection){
                element.removeDestination(dest);
            }
        }
        else{
            navigationController.remove(dest);
        }
        removeAllNavigationBars();
        updateAllNavigationBars();
    }

    public void replceDestination(String newFile, String oldFile){
        CompositDestination newDest = findDestination(newFile);

        Set<CompositDestination> overalCollection = new HashSet<CompositDestination>(allFiles);
        overalCollection.addAll(navigationController);

        for(CompositDestination element : overalCollection){
            element.replaceDestination(newDest, oldFile);
        }

        removeAllNavigationBars();
        updateAllNavigationBars();
    }



    private void recursiveRestoreRelationship(CompositDestination element, XmlTag tag){
        XmlAttribute attribute = tag.getAttribute("xrt:segue_destination");
        if(attribute != null){
            String segueDest = attribute.getValue();
            CompositDestination dest = findDestination(element.getFile().getParent().getVirtualFile().getPath() + File.separator + segueDest);
            element.updateDestination(null, dest);
        }

        for(PsiElement subTag : tag.getChildren()){
            if(subTag instanceof XmlTag){
                recursiveRestoreRelationship(element, (XmlTag) subTag);
            }
        }
    }

    public void updateDestination(VirtualFile file, String oldValue, String newValue){
        CompositDestination currentDestination = findDestination(file.getPath());
        CompositDestination oldDestination = oldValue != null ? findDestination(file.getParent().getPath() + File.separator + oldValue) : null;
        CompositDestination newDestination = newValue != null ? findDestination(file.getParent().getPath() + File.separator + newValue) : null;
        currentDestination.updateDestination(oldDestination, newDestination);

        removeAllNavigationBars();
        updateAllNavigationBars();
    }

    public void updateNavigationController(VirtualFile file, String oldValue, String newValue){
        CompositDestination currentDestination = findController(file.getPath());
        CompositDestination oldDestination = oldValue != null ? findDestination(file.getParent().getPath() + File.separator + oldValue) : null;
        CompositDestination newDestination = newValue != null ? findDestination(file.getParent().getPath() + File.separator + newValue) : null;
        currentDestination.updateDestination(oldDestination, newDestination);

        removeAllNavigationBars();
        updateAllNavigationBars();
    }

    private boolean isNavigationController(CompositDestination element){
        PsiFile file = element.getFile();
        if(file != null && file instanceof IXmlFile){
            XmlTag rootTag = ((IXmlFile) file).getRootTag();
            if(rootTag.getName().equals("com.android.sdklib.widgets.iOSNavigationController")){
                return true;
            }
        }
        return false;
    }

    private void removeAllNavigationBars(){
        for(CompositDestination element : allFiles){
            removeNavigationBar((IXmlFile) element.getFile());
        }
    }

    private void updateAllNavigationBars(){
        for(CompositDestination element : navigationController){
            element.addNavigationBarRecusively(new ArrayList<CompositDestination>());
        }
    }

    private void removeNavigationBar(final IXmlFile file){

        WriteCommandAction<Void> action = new WriteCommandAction<Void>(_project, "Add Resource", file) {
            @Override
            protected void run(@NotNull Result<Void> result) {
                XmlTag rootTag = file.getRootTag();
                boolean isNavBarExist = false;
                XmlTag navBarTag = null;
                for (XmlTag tag : rootTag.getSubTags()) {
                    XmlAttribute attribute = tag.getAttribute("android:id");
                    if (attribute != null && attribute.getValue().equals("MOE_navigationControllerBar")) {
                        isNavBarExist = true;
                        navBarTag = tag;
                    }
                }

                if (isNavBarExist) {
                    navBarTag.delete();
                }
            }
        };
        action.execute();
    }

    private void addNavigationBar(final IXmlFile file){
        WriteCommandAction<Void> action = new WriteCommandAction<Void>(_project, "Add Resource", file) {
            @Override
            protected void run(@NotNull Result<Void> result) {
                XmlTag rootTag = file.getRootTag();

                if(rootTag != null){
                    boolean isNavBarExist = false;

                    for(XmlTag tag : rootTag.getSubTags()){
                        XmlAttribute attribute = tag.getAttribute("android:id");
                        if(attribute != null){
                            isNavBarExist |= attribute.getValue().equals("MOE_navigationControllerBar");
                        }
                    }

                    if(!isNavBarExist){
                        XmlElementFactory factory = XmlElementFactory.getInstance(file.getProject());
                        XmlTagImpl linearLayout = (XmlTagImpl) factory.createTagFromText(XmlTagUtil.composeTagText("com.android.sdklib.widgets.base_elements.iOSNavigationBarContainer", ""));
                        linearLayout.setAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
                        linearLayout.setAttribute("android:layout_width", "fill_parent");
                        linearLayout.setAttribute("android:layout_height", "wrap_content");
                        linearLayout.setAttribute("android:orientation", "vertical");
                        linearLayout.setAttribute("android:id", "MOE_navigationControllerBar");

                        XmlTagImpl navBar = (XmlTagImpl) factory.createTagFromText(XmlTagUtil.composeTagText("com.android.sdklib.widgets.iOSUnEditableNavigationBar", ""));
                        navBar.setAttribute("android:layout_width", "fill_parent");
                        navBar.setAttribute("android:layout_height", "wrap_content");
                        navBar.setAttribute("style", "@style/XOSNavigationBar");
                        navBar.setAttribute("android:layout_weight", "1");

                        linearLayout.addSubTag(navBar, true);

                        rootTag.addSubTag(linearLayout,true);
                    }
                }
            }
        };
        action.execute();










    }

    private CompositDestination findDestination(String filePath){
        for(CompositDestination element : allFiles){
            String elementPath = FileUtil.toSystemDependentName(element.getFile().getVirtualFile().getPath());
            filePath = FileUtil.toSystemDependentName(filePath);
            if(elementPath.equals(filePath)){
                return element;
            }
        }
        return null;
    }
    private CompositDestination findController(String filePath){
        for(CompositDestination element : navigationController){
            String elementPath = FileUtil.toSystemDependentName(element.getFile().getVirtualFile().getPath());
            filePath = FileUtil.toSystemDependentName(filePath);
            if(elementPath.equals(filePath)){
                return element;
            }
        }
        return null;
    }
    //////////////////////////////////////////////////////////////////////////////////
    //Internal classes
    //////////////////////////////////////////////////////////////////////////////////
    private class CompositDestination{
        private List<CompositDestination> destinations;
        private PsiFile _psiFile;
        private Project _project;

        public CompositDestination(Project project, VirtualFile file) {
            destinations = new ArrayList<CompositDestination>();

            _project = project;
            _psiFile = IOSPsiUtils.getPsiFileSafely(project, file);
        }

        public CompositDestination(Project project, PsiFile file) {
            destinations = new ArrayList<CompositDestination>();

            _project = project;
            _psiFile = file;
        }
        public PsiFile getFile(){
            return _psiFile;
        }

        public void updateDestination(CompositDestination oldDest, CompositDestination newDest){
            if(oldDest != null){
                destinations.remove(oldDest);
            }

            if(newDest != null){
                destinations.add(newDest);
            }
        }

        public void removeDestination(CompositDestination oldDest){
            if(oldDest != null){
                destinations.remove(oldDest);
            }
            XmlTag rootTag = ((IXmlFile) _psiFile).getRootTag();
            if(rootTag != null){
                unsetSegue(oldDest, rootTag);
            }
        }

        public void replaceDestination(CompositDestination newDest, String oldSegue){
            if(newDest != null && destinations.contains(newDest)){
                XmlTag rootTag = ((IXmlFile) _psiFile).getRootTag();
                if(rootTag != null){
                    replaceSegue(newDest, oldSegue, rootTag);
                }
            }
        }

        public void clearDestination(){
            destinations.clear();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CompositDestination that = (CompositDestination) o;

            if (!_psiFile.getVirtualFile().getPath().equals(that._psiFile.getVirtualFile().getPath())) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = 31 * _psiFile.hashCode();
            return result;
        }

        private void unsetSegue(CompositDestination oldDest, XmlTag tag){
            XmlAttribute attribute = tag.getAttribute("xrt:segue_destination");
            if(attribute != null){
                if(attribute.getValue().equals(oldDest.getFile().getName())){
                    tag.setAttribute("xrt:segue_type", null);
                    tag.setAttribute("xrt:segue_destination", null);
                }
            }

            for(PsiElement subTag : tag.getChildren()){
                if(subTag instanceof XmlTag){
                    unsetSegue(oldDest, (XmlTag) subTag);
                }
            }
        }

        private void replaceSegue(CompositDestination newDest, String oldDest, XmlTag tag){
            XmlAttribute attribute = tag.getAttribute("xrt:segue_destination");
            if(attribute != null){
                if(attribute.getValue().equals(oldDest)){
                    tag.setAttribute("xrt:segue_destination", newDest.getFile().getName());
                }
            }

            for(PsiElement subTag : tag.getChildren()){
                if(subTag instanceof XmlTag){
                    replaceSegue(newDest, oldDest, (XmlTag) subTag);
                }
            }
        }

        private void addNavigationBarRecusively(List<CompositDestination> visited){
            if(_psiFile instanceof IXmlFile) {
                addNavigationBar((IXmlFile) _psiFile);
                visited.add(this);
            }

            for(CompositDestination c : destinations){
                if(!visited.contains(c)){
                    c.addNavigationBarRecusively(visited);
                }
            }
        }

    }
}
