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

import com.android.SdkConstants;
import com.android.resources.ResourceType;
import org.moe.designer.android.AndroidFacet;
import org.moe.designer.model.RadViewComponent;
import org.moe.designer.uipreview.ChooseClassDialog;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.designer.model.RadComponent;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class EventHandlerResourceDialog extends DialogWrapper implements TreeSelectionListener {
    private RadViewComponent myComponent;
    private Module myModule;
    private XmlTag myTag;


    private JPanel myContentPanel;
    private JLabel nsName;
    private JTextField nsValue;

    private JLabel errorMessage;

    private final String notUniqueMethod = "The method name is not unique";
    private final String notCorrecteMethod = "The method name is not correct";
    private final String emptyInitController = "Please set 'viewController' property for root layout";

    public EventHandlerResourceDialog(Module module, ResourceType[] types, String value, RadViewComponent component) {
        super(module.getProject());
        setAutoAdjustable(true);

        myComponent = component;
        myModule = module;
        myTag = component.getTag();

        setTitle("New Handler");

        AndroidFacet facet = AndroidFacet.getInstance(module);

        myContentPanel = new JPanel(new GridBagLayout());
        myContentPanel.setPreferredSize(new Dimension(400, 20));
        GridBagConstraints contentConstraints = new GridBagConstraints();
        contentConstraints.fill = GridBagConstraints.BOTH;
        contentConstraints.weightx = 1.0;

        //add nativeSelector name
        nsName = new JLabel();
        nsName.setText("Native Selector Name");
        myContentPanel.add(nsName, contentConstraints);

        nsValue = new JTextField();
        nsValue.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                errorMessage.setVisible(false);
            }
        });
        contentConstraints.gridwidth = GridBagConstraints.REMAINDER;
        myContentPanel.add(nsValue, contentConstraints);

        errorMessage = new JLabel();
        errorMessage.setForeground(Color.red);
//        contentConstraints.fill = GridBagConstraints.BOTH;
        contentConstraints.weightx = 0.0;
        errorMessage.setVisible(false);
        myContentPanel.add(errorMessage, contentConstraints);

        valueChanged(null);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myContentPanel;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {

    }

    @Override
    protected void doOKAction() {
        boolean isUnique = true;
        RadComponent root = myComponent.getRoot();
        if(root instanceof RadViewComponent){
            RadViewComponent rootView = (RadViewComponent)root;
            XmlAttribute attribute = rootView.getTag().getAttribute("xrt:viewController");
            if(attribute != null){
                String controllerClass = attribute.getValue();
                if(myModule != null && controllerClass != null && !controllerClass.isEmpty()){
                    PsiClass psiClass = ChooseClassDialog.findClass(myModule, controllerClass);
                    if(psiClass != null){
                        if(!isMethodUnique(psiClass, getResourceName())){
                            errorMessage.setText(notUniqueMethod);
                            errorMessage.setVisible(true);
                            validate();
                            repaint();
                            isUnique = false;
                        }
                        else if(!isNameCorrect(getResourceName())){
                            errorMessage.setText(notCorrecteMethod);
                            errorMessage.setVisible(true);
                            validate();
                            repaint();
                            isUnique = false;
                        }
                    }
                }
            }
            else{
                errorMessage.setText(emptyInitController);
                errorMessage.setVisible(true);
                isUnique = false;
            }
        }

        if(isUnique){
            super.doOKAction();
        }

    }

    public String getResourceName() {
        return nsValue.getText();
    }

    private boolean isNameCorrect(String methodName){
        return methodName.matches("([\\w]:?)+");
    }

    private boolean isMethodUnique(PsiClass psiClass, String methodName){
        boolean isMethodUnique = true;
        methodName = methodName != null && !methodName.isEmpty() && methodName.substring(methodName.length() - 1).equals(":") ? methodName : methodName + ":";
        for(PsiMethod method : psiClass.getMethods()){
            for(PsiAnnotation annotation : method.getModifierList().getAnnotations()){
                if(annotation.getChildren().length > 1 && annotation.getChildren()[1].getText().equals(SdkConstants.SELECTOR_ANNOTATION_PREFIX)){
                    if(annotation.getText().contains(String.format("(\"%s\")", methodName))){
                        isMethodUnique = false;
                    }
                }
            }
        }
        return isMethodUnique;
    }

}
