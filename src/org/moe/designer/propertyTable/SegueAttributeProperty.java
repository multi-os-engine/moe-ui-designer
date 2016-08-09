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

import com.android.resources.ResourceType;
import com.android.utils.XmlUtils;
import org.moe.designer.IOSDesignerEditorPanel;
import org.moe.designer.actions.SegueDestinationController;
import org.moe.designer.android.dom.AndroidDomUtil;
import org.moe.designer.android.dom.attrs.AttributeDefinition;
import org.moe.designer.android.dom.attrs.AttributeFormat;
import org.moe.designer.designSurface.RootView;
import org.moe.designer.ixml.IXmlFile;
import org.moe.designer.model.RadViewComponent;
import org.moe.designer.propertyTable.editors.FileResourceEditor;
import org.moe.designer.propertyTable.editors.ResourceEditor;
import org.moe.designer.utils.IOSPsiUtils;
import com.intellij.designer.model.Property;
import com.intellij.designer.propertyTable.PropertyEditor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Set;


public class SegueAttributeProperty extends AttributeProperty {
    private String _namespace;

    public SegueAttributeProperty(@NotNull String name, String namespace, @NotNull AttributeDefinition definition) {
        super(name, definition);
        _namespace = namespace;
    }

    public SegueAttributeProperty(@Nullable Property parent, @NotNull String name, String namespace, @NotNull AttributeDefinition definition){
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
            //update segue tree
            boolean isCorrectChoice = true;
            try{

                Component comp = component.getNativeComponent();
                if(comp instanceof RootView && myDefinition.getName().equals("segue_destination")){
                    IOSDesignerEditorPanel panel = ((RootView) comp).getPanel();
                    if(panel != null){
                        Project project = panel.getProject();
                        SegueDestinationController controller = SegueDestinationController.getInstance(project);
                        String oldValue = (String) getValue(component);

                        VirtualFile directory = panel.getVirtualFile().getParent();
                        if(!isNavigationController(project, directory, (String) value)){
                            if (isNavigationController(project, panel.getVirtualFile())) {
                                if(isRootView(component) || value == null){
                                    controller.updateNavigationController(panel.getVirtualFile(), oldValue, (String) value);
                                }else{
                                    isCorrectChoice = false;
                                }
                            }
                            else{
                                controller.updateDestination(panel.getVirtualFile(), oldValue, (String) value);
                            }
                        }
                    }
                }
            }
            catch (Exception e){

            }

            //write attribute value
            if(isCorrectChoice){
                if (StringUtil.isEmpty((String) value)) {
                    XmlAttribute attribute = getAttribute(component);
                    if (attribute != null) {
                        attribute.delete();
                    }
                }
                else {
                    String escapedValue = XmlUtils.toXmlAttributeValue((String)value);
                    component.getTag().setAttribute(String.format("%s:%s",_namespace, myDefinition.getName()), escapedValue);
                }
            }

            }
        });
    }

    @Override
    public Property<RadViewComponent> createForNewPresentation(@Nullable Property parent, @NotNull String name) {
        return new SegueAttributeProperty(parent, name, _namespace, myDefinition);
    }

    @Override
    protected PropertyEditor createResourceEditor(AttributeDefinition definition, Set<AttributeFormat> formats) {

        if(definition.getName().equals("segue_identifier")){
            FileResourceEditor editor = new FileResourceEditor(new ResourceType[]{ResourceType.IXML}, formats, definition.getValues());
            editor.setEnabledBrowsButton(false);
            return  editor;
        }
        else{
            return new FileResourceEditor(new ResourceType[]{ResourceType.IXML}, formats, definition.getValues());
        }


    }

    @Nullable
    private XmlAttribute getAttribute(RadViewComponent component) {
        return component.getTag().getAttribute(String.format("%s:%s", _namespace, myDefinition.getName()));
    }

    private boolean isNavigationController(Project project, VirtualFile file){
        VirtualFile directory = file.getParent();
        return isNavigationController(project, directory, file.getName());
    }

    private boolean isNavigationController(Project project, VirtualFile directory, String fileName){
        if(fileName != null && !fileName.isEmpty()){
            PsiDirectory psiDirectory = IOSPsiUtils.getPsiDirectorySafely(project, directory);
            PsiFile file = psiDirectory.findFile(fileName);
            if(file != null && file instanceof IXmlFile){
                XmlTag rootTag = ((IXmlFile) file).getRootTag();
                if(rootTag != null && rootTag.getName().equals("com.android.sdklib.widgets.iOSNavigationController"))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRootView(RadViewComponent component){
        XmlTag tag = component.getTag();
        if(tag != null){
            XmlAttribute attribute = tag.getAttribute(String.format("%s:%s", _namespace, "segue_type"));
            if(attribute != null){
                return attribute.getValue().equals("relationship");
            }
        }
        return false;
    }


}
