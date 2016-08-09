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

package org.moe.designer;

import org.moe.designer.model.RadViewComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

public class SampleTest extends TestBase {

    public void testSimple() throws Exception {
        init();
        IOSDesignerEditorPanel editor = createLayoutEditor(getTestFile("simple.ixml"));
        RadViewComponent rootComponent = editor.getRootViewComponent();
        assertNotNull(rootComponent);
        String testString = "Device Screen\n" +
                "    LinearLayout";
        assertEquals(testString,
                printTree(rootComponent, false));
        testString = "RadViewComponent{tag=<LinearLayout>, bounds=[0,0:640x960}\n" +
                "    RadViewContainer{tag=<LinearLayout>, bounds=[0,75:640x885}";
        assertEquals(testString,
                printTree(rootComponent, true));
    }

    public void testSimple2() throws Exception {
        init();
        IOSDesignerEditorPanel editor = createLayoutEditor(getTestFile("simple2.ixml"));
        RadViewComponent rootComponent = editor.getRootViewComponent();
        assertNotNull(rootComponent);
        String testString = "Device Screen\n" +
                "    LinearLayout (vertical)\n" +
                "        Button - \"My Button\"\n" +
                "        Label - \"My TextView\"";
        assertEquals(testString,
                printTree(rootComponent, false));
        testString = "RadViewComponent{tag=<LinearLayout>, bounds=[0,0:640x960}\n" +
                "    RadViewContainer{tag=<LinearLayout>, bounds=[0,75:640x885}\n" +
                "        RadViewComponent{tag=<com.android.sdklib.widgets.iOSButton>, bounds=[0,75:640x200}\n" +
                "        RadViewComponent{tag=<com.android.sdklib.widgets.iOSTextView>, bounds=[0,275:400x220}";
        assertEquals(testString,
                printTree(rootComponent, true));
    }

    protected IOSDesignerEditorPanel createLayoutEditor(VirtualFile xmlFile) {
        Project project = getProject();
        IOSDesignerEditor editor = new IOSDesignerEditor(project, xmlFile);
        editor.findModule(project, xmlFile);
        IOSDesignerEditorPanel panel = (IOSDesignerEditorPanel)editor.getDesignerPanel();
        panel.requestImmediateRender();
        Disposer.register(project, editor, project.getName());
        return panel;
    }

}
