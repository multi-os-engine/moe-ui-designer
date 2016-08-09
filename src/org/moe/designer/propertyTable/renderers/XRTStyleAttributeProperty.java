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

package org.moe.designer.propertyTable.renderers;

import com.android.utils.XmlUtils;
import org.moe.designer.android.dom.attrs.AttributeDefinition;
import org.moe.designer.designSurface.RootView;
import org.moe.designer.model.RadViewComponent;
import org.moe.designer.model.ViewsMetaManager;
import com.intellij.designer.model.MetaModel;
import com.intellij.designer.model.Property;
import com.intellij.designer.model.RadComponent;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XRTStyleAttributeProperty extends XRTAttributeProperty {
    private enum StyleTagsMatcher{


    }



    public XRTStyleAttributeProperty(@NotNull String name, @NotNull AttributeDefinition definition) {
        super(name, definition);
    }

    public XRTStyleAttributeProperty(@NotNull String name, String namespace, @NotNull AttributeDefinition definition) {
        super(name, namespace, definition);
    }

    public XRTStyleAttributeProperty(@Nullable Property parent, @NotNull String name, String namespace, @NotNull AttributeDefinition definition) {
        super(parent, name, namespace, definition);
    }

    @Override
    public void setValue(@NotNull final RadViewComponent component, final Object value) throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                if (StringUtil.isEmpty((String) value)) {
                    XmlAttribute attribute = getAttribute(component);
                    if (attribute != null) {
                        attribute.delete();
//                        removeChildren(component);
                    }
                } else {
                    removeChildren(component);
                    if (!value.equals("Custom")) {
//                        addAppropriateContent(component, value.toString());
                    }


                    String escapedValue = XmlUtils.toXmlAttributeValue((String) value);
                    component.getTag().setAttribute(String.format("%s:%s", _namespace, myDefinition.getName()), escapedValue);
                }
            }
        });
    }

    private void removeChildren(RadViewComponent component){
        List<RadComponent>children = new ArrayList<RadComponent>(component.getChildren());
        for(RadComponent child : children){
            if(child instanceof RadViewComponent){
                component.remove(child);
            }
        }
        XmlTag[] subTags = component.getTag().getSubTags();
        for(XmlTag subTag : subTags){
            subTag.delete();
        }
    }

    private void addAppropriateContent(RadViewComponent component, String value){
        Component view = component.getNativeComponent();
        if(view instanceof RootView) {
            final RootView rootView = (RootView) view;
            ViewsMetaManager manager = ViewsMetaManager.getInstance(rootView.getPanel().getProject());
//            MetaModel labelMetaModel = manager.getModelByTag();






        }
    }
}
