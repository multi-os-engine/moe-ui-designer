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
import org.moe.designer.android.dom.converters.OnClickConverter;
import org.moe.designer.model.RadModelBuilder;
import org.moe.designer.propertyTable.renderers.EventHandlerEditorRenderer;
import org.moe.designer.propertyTable.renderers.ViewControllerEditorRenderer;
import org.moe.designer.uipreview.ChooseClassDialog;
import org.moe.designer.utils.IOSPsiUtils;
import com.intellij.designer.model.PropertiesContainer;
import com.intellij.designer.model.PropertyContext;
import com.intellij.designer.model.RadComponent;
import com.intellij.designer.model.RadPropertyContext;
import com.intellij.designer.propertyTable.InplaceContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class ViewControllerEditor extends ResourceEditor {
    protected static final ResourceType[] TYPES = {ResourceType.STRING};
    protected static final Set<AttributeFormat> FORMATS = EnumSet.of(AttributeFormat.String, AttributeFormat.Enum);
    protected boolean isHandler = false;

    public ViewControllerEditor() {
        super(TYPES, FORMATS, ArrayUtil.EMPTY_STRING_ARRAY);
        getCombo().setRenderer(new ViewControllerEditorRenderer());
    }

    public ViewControllerEditor(Set<AttributeFormat> formats) {
        super(TYPES, FORMATS, ArrayUtil.EMPTY_STRING_ARRAY);
        if(formats.contains(AttributeFormat.Handler)){
            isHandler = true;
        }
        myEditor.setButtonVisible(false);
        getCombo().setRenderer(new ViewControllerEditorRenderer());
    }


    @Override
    public Object getValue() {
        Object item = getCombo().getSelectedItem();
        if (item instanceof PsiClassWrapper) {
            return ((PsiClassWrapper)item).getPsiClass().getQualifiedName();
        }
        return super.getValue();
    }

    @NotNull
    @Override
    public JComponent getComponent(@Nullable PropertiesContainer container,
                                   @Nullable PropertyContext context,
                                   Object value,
                                   @Nullable InplaceContext inplaceContext) {
        myComponent = (RadComponent)container;
        myRootComponent = context instanceof RadPropertyContext ? ((RadPropertyContext)context).getRootComponent() : null;

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(StringsComboEditor.UNSET);

        JComboBox combo = getCombo();
        combo.setModel(model);

        Module module = RadModelBuilder.getModule(myRootComponent);
        Set<String> names = new HashSet<String>();

        if (module != null) {
            List<VirtualFile> allFiles = getAllFiles(module.getModuleFile().getParent().getChildren());
            for(VirtualFile vFile : allFiles){
                try{
                    PsiElement[] elements = IOSPsiUtils.getPsiFileSafely(module.getProject(), vFile).getChildren();
                    PsiClass psiClass = null;
                    for(PsiElement element : elements){
                        if(element instanceof PsiClass){
                            psiClass = (PsiClass)element;
                        }
                    }
                    if(psiClass != null){
                        model.addElement(new PsiClassWrapper(psiClass));
                    }
                }
                catch (Exception e){

                }
            }
        }

        combo.setSelectedItem(value);
        return myEditor;
    }

    private List<VirtualFile> getAllFiles(VirtualFile[] files){
        List<VirtualFile> javaFiles = new ArrayList<VirtualFile>();
        for(VirtualFile file : files){
            if(file instanceof VirtualDirectoryImpl){
                javaFiles.addAll(getAllFiles(file.getChildren()));
            }
            else if(file instanceof VirtualFileImpl){
                if(file.getExtension() != null && file.getExtension().equals("java")){
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }


    protected JComboBox getCombo() {
        return (JComboBox)myEditor.getChildComponent();
    }

    public static final class PsiClassWrapper {
        private final PsiClass myClass;

        public PsiClassWrapper(PsiClass psiClass) {
            myClass = psiClass;
        }

        public PsiClass getPsiClass() {
            return myClass;
        }

        @Override
        public boolean equals(Object object) {
            return object == this || myClass.getName().equals(object);
        }

        @Override
        public int hashCode() {
            return myClass.getName().hashCode();
        }

        @Override
        public String toString() {
            return myClass.getName();
        }
    }
}
