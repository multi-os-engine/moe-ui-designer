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

import org.moe.designer.android.dom.attrs.AttributeFormat;
import com.intellij.designer.model.RadComponent;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;

import java.util.Set;

public class ViewControllerRenderer extends ResourceRenderer {
    public ViewControllerRenderer(Set<AttributeFormat> formats) {
        super(formats);
    }

    @Override
    protected void formatValue(RadComponent component, String value) {
        super.formatValue(component, value);
        if (!StringUtil.isEmpty(value)) {
            myColoredComponent.setIcon(AllIcons.Nodes.Class);
        }
    }
}
