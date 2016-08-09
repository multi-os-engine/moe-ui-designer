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

package org.moe.designer.propertyTable.editors;

import com.android.resources.ResourceType;
import org.moe.designer.android.dom.attrs.AttributeFormat;
import org.moe.designer.model.RadModelBuilder;
import org.moe.designer.model.RadViewComponent;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FileResourceEditor extends ResourceEditor {
    public FileResourceEditor(Set<AttributeFormat> formats, String[] values) {
        super(formats, values);
    }

    public FileResourceEditor(@Nullable ResourceType[] types, Set<AttributeFormat> formats, @Nullable String[] values) {
        super(types, formats, values);
    }

    public void setEnabledBrowsButton(boolean enabled){
        myEditor.setButtonVisible(enabled);
    }

    @Override
    protected void showDialog() {
        Module module = RadModelBuilder.getModule(myRootComponent);
        if (module == null) {
            if (myBooleanResourceValue != null) {
                fireEditingCancelled();
            }
            return;
        }
        ResourceDialog dialog = new ResourceDialog(module, myTypes, (String)getValue(), (RadViewComponent)myComponent);
        if (dialog.showAndGet()) {
            setValue(dialog.getResourceName());
        }
        else {
            if (myBooleanResourceValue != null) {
                fireEditingCancelled();
            }
        }
    }
}
