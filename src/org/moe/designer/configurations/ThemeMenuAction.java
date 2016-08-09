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

package org.moe.designer.configurations;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ThemeMenuAction extends FlatComboAction {
    private final RenderContext myRenderContext;

    public ThemeMenuAction(@NotNull RenderContext renderContext) {
        myRenderContext = renderContext;
        Presentation presentation = getTemplatePresentation();
        presentation.setDescription("The virtual device to render the layout with");
        presentation.setIcon(AndroidIcons.Themes);
        updatePresentation(presentation);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        updatePresentation(e.getPresentation());
    }

    @NotNull
    @Override
    protected DefaultActionGroup createPopupActionGroup(JComponent button) {
        return null;
    }

    private void updatePresentation(Presentation presentation) {
        Configuration configuration = myRenderContext.getConfiguration();
        boolean visible = configuration != null;
        if (visible) {
            String label = configuration.getTheme();
            presentation.setText(label);
        }
        if (visible != presentation.isVisible()) {
            presentation.setVisible(visible);
        }
    }
}
