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

import com.android.SdkConstants;
import com.android.utils.XmlUtils;
import org.moe.designer.android.dom.attrs.AttributeDefinition;
import org.moe.designer.model.RadViewComponent;
import org.moe.designer.propertyTable.AttributeProperty;
import com.intellij.designer.model.Property;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XRTAttributeProperty extends AttributeProperty implements Cloneable {
    protected String _namespace;

    public XRTAttributeProperty(@NotNull String name, @NotNull AttributeDefinition definition) {
        super(name, definition);
        _namespace = SdkConstants.XRT_NAMESPACE;
    }

    public XRTAttributeProperty(@NotNull String name, String namespace, @NotNull AttributeDefinition definition) {
        super(name, definition);
        _namespace = namespace;
    }

    public XRTAttributeProperty(@Nullable Property parent, @NotNull String name, String namespace, @NotNull AttributeDefinition definition) {
        super(parent, name, definition);
        _namespace = namespace;
    }

    @Override
    public Object getValue(@NotNull RadViewComponent component) throws Exception {
        XmlAttribute attribute = getAttribute(component);
        if (attribute != null) {
            String attributeValue = attribute.getValue();
            if (attributeValue != null) {
                return XmlUtils.fromXmlAttributeValue(attributeValue);
            }
        }

        return "";
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
                    }
                }
                else {
                    String escapedValue = XmlUtils.toXmlAttributeValue((String)value);
                    component.getTag().setAttribute(String.format("%s:%s", _namespace, myDefinition.getName()), escapedValue);
                }
            }
        });
    }

    @Override
    public XRTAttributeProperty clone(){
        return new XRTAttributeProperty(getParent(), getName(),_namespace, myDefinition);
    }

    @Nullable
    protected XmlAttribute getAttribute(RadViewComponent component) {
        return component.getTag().getAttribute(String.format("%s:%s", _namespace, myDefinition.getName()));
    }
}
