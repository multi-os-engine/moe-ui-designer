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

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextComponentAccessor;

import javax.swing.*;
import java.awt.*;


public class ComboboxWithBrowseButton extends ComponentWithBrowseButton<JComboBox> {
    public ComboboxWithBrowseButton() {
        super(new JComboBox(), null);
    }

    public ComboboxWithBrowseButton(JComboBox comboBox) {
        super(comboBox, null);
    }

    public JComboBox getComboBox() {
        return getChildComponent();
    }

    @Override
    public void setTextFieldPreferredWidth(final int charCount) {
        super.setTextFieldPreferredWidth(charCount);
        final Component comp = getChildComponent().getEditor().getEditorComponent();
        Dimension size = comp.getPreferredSize();
        FontMetrics fontMetrics = comp.getFontMetrics(comp.getFont());
        size.width = fontMetrics.charWidth('a') * charCount;
        comp.setPreferredSize(size);
    }

    public void addBrowseFolderListener(Project project, FileChooserDescriptor descriptor) {
        addBrowseFolderListener(null, null, project, descriptor, TextComponentAccessor.STRING_COMBOBOX_WHOLE_TEXT);
    }
}