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

package org.moe.designer.propertyTable;

import com.android.utils.XmlUtils;
import org.moe.designer.android.dom.attrs.AttributeDefinition;
import org.moe.designer.model.RadViewComponent;
import org.moe.designer.utils.AndroidWidgetUtils;
import com.intellij.designer.model.RadComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class OrientationAttributeProperty extends AttributeProperty {

    public OrientationAttributeProperty(@NotNull String name, @NotNull AttributeDefinition definition) {
        super(name, definition);
    }

    @Override
    public void setValue(@NotNull final RadViewComponent component, final Object value) throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                if (AndroidWidgetUtils.isResizable(component, myDefinition.getName())) {
                    if (StringUtil.isEmpty((String) value)) {
                        XmlAttribute attribute = getAttribute(component);
                        if (attribute != null) {
                            attribute.delete();
                        }
                    } else {

                        if (value.equals("vertical")) {
                            clearWeight(component, "layout_width", "layout_height");
                        } else if (value.equals("horizontal")) {
                            clearWeight(component, "layout_height", "layout_width");
                        }

                        String namespace = getNamespace(component, true);
                        String escapedValue = XmlUtils.toXmlAttributeValue((String) value);
                        component.getTag().setAttribute(myDefinition.getName(), namespace, escapedValue);
                    }
                }

            }
        });
    }

    @Nullable
    private XmlAttribute getAttribute(RadViewComponent component) {
        return component.getTag().getAttribute(myDefinition.getName(), getNamespace(component, false));
    }

    private void clearWeight(RadViewComponent container, String deleteProperty, String setProperty){
        String fillParent = "fill_parent";
        String matchParent = "match_parent";

        List<RadComponent> children = container.getChildren();
        for(RadComponent child : children){
            if(child instanceof RadViewComponent){
                XmlTag tag = ((RadViewComponent) child).getTag();
                if(tag != null){

                    //delete attribute
                    XmlAttribute deleteAttribute = tag.getAttribute(deleteProperty, getNamespace(((RadViewComponent) child), false));
                    if(deleteAttribute != null){
                        String attributeValue = deleteAttribute.getValue();

                        if(attributeValue.equals(fillParent) || attributeValue.equals(matchParent)){
                            XmlAttribute weightAttribute = tag.getAttribute("layout_weight", getNamespace(((RadViewComponent) child), false));
                            if(weightAttribute != null){
                                weightAttribute.delete();
                            }
                        }
                    }

                    //set attribute
                    XmlAttribute setAttribute = tag.getAttribute(setProperty, getNamespace(((RadViewComponent) child), false));
                    if(setAttribute != null){
                        String attributeValue = setAttribute.getValue();

                        if(attributeValue.equals(fillParent) || attributeValue.equals(matchParent)){
                            tag.setAttribute("layout_weight", getNamespace(((RadViewComponent) child), false), "1.0");
                        }
                    }
                }
            }
        }
    }

}
