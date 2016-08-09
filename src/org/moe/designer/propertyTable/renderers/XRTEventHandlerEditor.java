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

import com.android.SdkConstants;
import com.android.resources.ResourceType;
import org.moe.designer.android.dom.attrs.AttributeFormat;
import org.moe.designer.android.dom.converters.OnClickConverter;
import org.moe.designer.model.RadModelBuilder;
import org.moe.designer.model.RadViewComponent;
import org.moe.designer.propertyTable.editors.EventHandlerResourceDialog;
import org.moe.designer.propertyTable.editors.ResourceDialog;
import org.moe.designer.propertyTable.editors.ResourceEditor;
import org.moe.designer.propertyTable.editors.StringsComboEditor;
import org.moe.designer.uipreview.ChooseClassDialog;
import org.moe.designer.utils.IOSPsiUtils;
import com.intellij.designer.model.PropertiesContainer;
import com.intellij.designer.model.PropertyContext;
import com.intellij.designer.model.RadComponent;
import com.intellij.designer.model.RadPropertyContext;
import com.intellij.designer.propertyTable.InplaceContext;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.tree.java.PsiAnnotationParamListImpl;
import com.intellij.psi.impl.source.tree.java.PsiNameValuePairImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class XRTEventHandlerEditor extends ResourceEditor {
    private static final ResourceType[] TYPES = {ResourceType.STRING};
    private static final Set<AttributeFormat> FORMATS = EnumSet.of(AttributeFormat.String, AttributeFormat.Enum);

    public XRTEventHandlerEditor() {
        super(TYPES, FORMATS, ArrayUtil.EMPTY_STRING_ARRAY);
        getCombo().setRenderer(new XRTEventHandlerEditorRenderer());
    }

    public XRTEventHandlerEditor(Set<AttributeFormat> formats, boolean browsButtonNedded) {
        super(TYPES, FORMATS, ArrayUtil.EMPTY_STRING_ARRAY);
        if(!browsButtonNedded){
            myEditor.setButtonVisible(false);
        }
        getCombo().setRenderer(new XRTEventHandlerEditorRenderer());
    }

    @Override
    public Object getValue() {
        Object item = getCombo().getSelectedItem();
        if (item instanceof PsiMethodWrapper) {
//            return ((PsiMethodWrapper)item).getMyName();
            return item.toString();
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
            if(myComponent.getRoot() instanceof RadViewComponent){
                XmlAttribute attribute = ((RadViewComponent) myComponent.getRoot()).getTag().getAttribute("xrt:viewController");
                if(attribute != null){
                    String className = attribute.getValue();
                    PsiClass psiClass = ChooseClassDialog.findClass(module, className);
                    if(psiClass != null){
                        for(PsiMethod method : psiClass.getMethods()){
                            boolean isNativeSelector = false;
                            boolean isProperty = false;
                            String newName = "";

                            for(PsiAnnotation annotation : method.getModifierList().getAnnotations()){
                                if(annotation.getChildren().length > 1 && annotation.getChildren()[1].getText().equals(SdkConstants.SELECTOR_ANNOTATION_PREFIX)){
                                    isNativeSelector = true;
                                    for(PsiElement element : annotation.getChildren()){
                                        if(element instanceof PsiAnnotationParamListImpl){
                                            for(PsiElement anElement : ((PsiAnnotationParamListImpl)element).getChildren()){
                                                if(anElement instanceof PsiNameValuePairImpl){
                                                    newName = anElement.getText().replaceAll("[\"]", "");
                                                }
                                            }
                                        }
                                    }
                                }
                                if(annotation.getChildren().length > 1 && annotation.getChildren()[1].getText().equals(SdkConstants.PROPERTY_ANNOTATION_PREFIX)){
                                    isProperty = true;
                                }
                            }

                            if(isNativeSelector && !isProperty){
                                model.addElement(new PsiMethodWrapper(method, newName));
                            }
                        }
                    }
                }
            }
        }

        combo.setSelectedItem(value);
        return myEditor;
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
        EventHandlerResourceDialog dialog = new EventHandlerResourceDialog(module, myTypes, (String)getValue(), (RadViewComponent)myComponent);
        if (dialog.showAndGet() && !dialog.getResourceName().isEmpty()) {
            setValue(dialog.getResourceName());
        }
        else {
            if (myBooleanResourceValue != null) {
                fireEditingCancelled();
            }
        }
    }

    protected JComboBox getCombo() {
        return (JComboBox)myEditor.getChildComponent();
    }

    public static final class PsiMethodWrapper {
        private final PsiMethod myMethod;
        private final String myName;

        public PsiMethodWrapper(PsiMethod method, String newName)
        {
            myMethod = method;
            myName = newName;
        }

        public String getMyName() {
            return myName;
        }

        public PsiMethod getMethod() {
            return myMethod;
        }

        @Override
        public boolean equals(Object object) {
            return object == this || myMethod.getName().equals(object);
        }

        @Override
        public int hashCode() {
            return myMethod.getName().hashCode();
        }

        @Override
        public String toString() {
            return myName;
        }
    }
}
