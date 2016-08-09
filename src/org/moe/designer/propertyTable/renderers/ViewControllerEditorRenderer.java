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

import org.moe.designer.propertyTable.editors.StringsComboEditor;
import org.moe.designer.propertyTable.editors.ViewControllerEditor;
import com.intellij.designer.propertyTable.PropertyTable;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import com.intellij.ui.ColoredListCellRenderer;

import javax.swing.*;

public class ViewControllerEditorRenderer extends ColoredListCellRenderer {
    @Override
    protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        clear();
        PropertyTable.updateRenderer(this, selected);

        if (value == StringsComboEditor.UNSET) {
            append(StringsComboEditor.UNSET);
        }
        else if (value instanceof ViewControllerEditor.PsiClassWrapper) {
            PsiClass psiClass = ((ViewControllerEditor.PsiClassWrapper)value).getPsiClass();
            setIcon(psiClass.getIcon(Iconable.ICON_FLAG_VISIBILITY));
            append(psiClass.getQualifiedName());
        }
    }
}
