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

import com.intellij.designer.model.RadComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RadNavigationViewContainer extends RadViewContainer {

    @Override
    public void add(@NotNull RadComponent component, @Nullable RadComponent insertBefore) {
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
            else{
                index = children.indexOf(insertBefore);
                children.add(index, component);
            }

        }

        if (getLayout() != null) {
            getLayout().addComponentToContainer(component, index);
        }
    }
}
