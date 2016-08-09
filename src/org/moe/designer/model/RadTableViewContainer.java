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

package org.moe.designer.model;

import org.moe.designer.designSurface.RootView;
import org.moe.designer.ixml.IXmlFile;
import com.intellij.designer.model.MetaModel;
import com.intellij.designer.model.RadComponent;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;


public class RadTableViewContainer extends RadViewContainer {

    @Override
    public boolean canDelete() {
        return !isBackground() && !isTableViewController() && super.canDelete();
    }

    @Override
    public void add(final @NotNull RadComponent component, @Nullable RadComponent insertBefore) {
        component.setParent(this);

        int index;
        List<RadComponent> children = getChildren();
        if (insertBefore == null) {
            index = children.size();
            children.add(component);
        }
        else {
            if(insertBefore instanceof RadViewContainer
                    && ((RadViewContainer) insertBefore).getTag().getName().equals("com.android.sdklib.widgets.base_elements.iOSNavigationBarContainer")){
                index = children.indexOf(insertBefore) + 1;
                children.add(index, component);
            }
            else if(insertBefore instanceof RadViewComponent
                    && ((RadViewComponent) insertBefore).getTag().getName().equals("com.android.sdklib.widgets.iOSTextView")){
                index = children.indexOf(insertBefore) + 1;
                children.add(index, component);
            }
            else{
                index = children.indexOf(insertBefore);
                children.add(index, component);
            }

        }

        if (getLayout() != null) {
            getLayout().addComponentToContainer(component, index);
        }
    }

    @Override
    public void remove(@NotNull RadComponent component){
        super.remove(component);

        List<RadComponent>children = getChildren();
        if(children.size() == 1){
            RadComponent child = children.get(0);
            if(child instanceof RadViewComponent){
                if(((RadViewComponent)child).getTag().getName().equals("com.android.sdklib.widgets.iOSTextView")){
                    try {
                        child.delete();
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    private boolean isTableViewController(){
        RadComponent parent = getParent();
        if(parent != null){
            if(parent instanceof RadViewContainer){
                String tagName = ((RadViewContainer)parent).getTag().getName();
                return tagName.equals("com.android.sdklib.widgets.iOSTableViewController");
            }
        }
        return false;
    }
}
