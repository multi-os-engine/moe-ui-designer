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

import com.intellij.designer.model.PropertiesContainer;
import com.intellij.designer.model.PropertyContext;
import com.intellij.designer.propertyTable.PropertyRenderer;
import com.intellij.designer.propertyTable.PropertyTable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class HandlerRenderer extends JPanel implements PropertyRenderer {
    protected final JCheckBox myCheckBox;
    protected final JLabel myText;
    private boolean isErrorLabel = false;

    public HandlerRenderer() {
        myCheckBox = new JCheckBox();
        myText = new JLabel();

        setLayout(new BorderLayout());
        add(myCheckBox, BorderLayout.WEST);
        add(myText, BorderLayout.CENTER);
    }

    @NotNull
    public JComponent getComponent(@Nullable PropertiesContainer container,
                                   PropertyContext context,
                                   @Nullable Object value,
                                   boolean selected,
                                   boolean hasFocus) {
        PropertyTable.updateRenderer(this, selected);

        List<String> values = StringUtil.split(value.toString(), "-");

        PropertyTable.updateRenderer(myCheckBox, selected);
        myCheckBox.setSelected(value != null && Boolean.valueOf(values.get(0)));

        String text = values.get(1).equals(" ") ? "" : values.get(1);
        myText.setText(text);
        if(Boolean.valueOf(values.get(2))){
            myText.setForeground(Color.red);
            isErrorLabel = true;
        }
        else{
            myText.setForeground(UIUtil.getLabelForeground());
            isErrorLabel=false;
        }

        return this;
    }

    public String getHandlerName(){
        return myText.getText();
    }

    public Boolean isHandlerChecked(){
        return myCheckBox.isSelected();
    }

    public Boolean isHandlerError(){
        return isErrorLabel;
    }
}
