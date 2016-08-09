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

import com.android.SdkConstants;
import com.android.utils.XmlUtils;
import org.moe.designer.android.dom.attrs.AttributeDefinition;
import org.moe.designer.android.dom.attrs.AttributeFormat;
import org.moe.designer.designSurface.RootView;
import org.moe.designer.model.RadModelBuilder;
import org.moe.designer.model.RadViewComponent;
import org.moe.designer.uipreview.ChooseClassDialog;
import com.intellij.codeInsight.generation.*;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInspection.ex.GlobalInspectionContextBase;
import com.intellij.designer.model.Property;
import com.intellij.designer.model.RadComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actions.EnterAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.PsiElementFactoryImpl;
import com.intellij.psi.impl.light.LightClassReference;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.impl.source.tree.java.PsiAnnotationImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IBActionAttributeProperty extends AttributeProperty {
    private String _namespace;

    public IBActionAttributeProperty(@NotNull String name, String namespace, @NotNull AttributeDefinition definition) {
        super(name, definition);
        _namespace = namespace;
    }

    public IBActionAttributeProperty(@Nullable Property parent, @NotNull String name, String namespace, @NotNull AttributeDefinition definition){
        super(parent, name, definition);
        _namespace = namespace;
    }

    @Override
    public Object getValue(@NotNull RadViewComponent component) throws Exception {
        XmlAttribute attribute = getAttribute(component);
        if (attribute != null) {
            String value = attribute.getValue();
            List<String> values = StringUtil.split(value, "|");
            for(String val : values){
                String eventName = StringUtil.split(val, "-").get(0);
                if(eventName.equals(myDefinition.getName())){
                    return XmlUtils.fromXmlAttributeValue(StringUtil.split(val, "-").get(1));
                }
            }
        }

        return "";
    }

    @Override
    public void setValue(@NotNull final RadViewComponent component, final Object value) throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                if (StringUtil.isEmpty((String)value)) {
                    XmlAttribute attribute = getAttribute(component);
                    if (attribute != null) {
                        String value = attribute.getValue();
                        List<String> values = StringUtil.split(value, "|");
                        List<String> newValues = new ArrayList<String>();
                        for (String val : values){
                            String eventName = StringUtil.split(val, "-").get(0);
                            if(!eventName.equals(myDefinition.getName())){
                                newValues.add(val);
                            }
                        }

                        if(newValues.size() > 0){
                            String escapedValue = XmlUtils.toXmlAttributeValue(StringUtil.join(newValues, "|"));
                            component.getTag().setAttribute(_namespace, escapedValue);
                        }else{
                            attribute.delete();
                        }
                    }
                }
                else {
                    RadComponent root = component.getRoot();
                    if(root instanceof RadViewComponent) {
                        RadViewComponent rootView = (RadViewComponent) root;
                        XmlAttribute attributeIC = rootView.getTag().getAttribute("xrt:viewController");
                        if(attributeIC != null){
                            String escapedValue = XmlUtils.toXmlAttributeValue((String) value);

                            if(isNameCorrect(escapedValue)){
                                XmlAttribute attribute = getAttribute(component);

                                String attrValue = "";
                                if(attribute != null){
                                    attrValue = attribute.getValue();
                                }

                                List<String> values = StringUtil.split(attrValue, "|");
                                List<String> newValue = new ArrayList<String>();
                                boolean isEventContained = false;
                                for(String val : values){
                                    String eventName = StringUtil.split(val, "-").get(0);
                                    if(eventName.equals(myDefinition.getName())){
                                        escapedValue = escapedValue.substring(escapedValue.length() - 1).equals(":") ? escapedValue : escapedValue + ":";
                                        escapedValue = String.format("%s-%s", myDefinition.getName(), escapedValue);
                                        newValue.add(escapedValue);
                                        isEventContained = true;
                                    }
                                    else{
                                        val = val.substring(val.length() - 1).equals(":") ? val : val + ":";
                                        newValue.add(val);
                                    }
                                }

                                if(!isEventContained){
                                    escapedValue = escapedValue.substring(escapedValue.length() - 1).equals(":") ? escapedValue : escapedValue + ":";
                                    escapedValue = String.format("%s-%s", myDefinition.getName(), escapedValue);
                                    newValue.add(escapedValue);
                                }
                                escapedValue = XmlUtils.toXmlAttributeValue(StringUtil.join(newValue, "|"));
                                component.getTag().setAttribute(_namespace, escapedValue);

                                addStubToViewController(component, value);
                            }
                        }
                    }
                }
            }
        });
    }

    public Property<RadViewComponent> createForNewIBPresentation(@Nullable Property parent, @NotNull String name, String namespace) {
        return new IBActionAttributeProperty(parent, name, namespace, myDefinition);
    }

    @Nullable
    private XmlAttribute getAttribute(RadViewComponent component) {
        return component.getTag().getAttribute(_namespace);
    }

    private boolean isNameCorrect(String methodName){
        return methodName.matches("([\\w]:?)+");
    }

    private void addStubToViewController(RadViewComponent component, Object value){
        RadComponent root = component.getRoot();
        if(root instanceof RadViewComponent){
            RadViewComponent rootView = (RadViewComponent)root;
            Module module = RadModelBuilder.getModule(rootView);
            String controllerClass = rootView.getTag().getAttribute("xrt:viewController").getValue();
            if(module != null && controllerClass != null && !controllerClass.isEmpty()){
                PsiClass psiClass = ChooseClassDialog.findClass(module, controllerClass);
                if(psiClass != null && isMethodUnique(psiClass, value.toString())){
                    Document javaClass = psiClass.getContainingFile().getViewProvider().getDocument();
                    Project project = module.getProject();
                    Editor editor = EditorFactory.getInstance().createEditor(javaClass, project, psiClass.getContainingFile().getVirtualFile(), false);
                    doGenerate(project, editor, psiClass, new ClassMember[]{}, value.toString());

                    project.save();
                }
            }
        }
    }

    private void doGenerate(final Project project, final Editor editor, PsiClass aClass, ClassMember[] members, String methodName) {
        editor.getCaretModel().moveToOffset(editor.getDocument().getText().lastIndexOf("}") - 1);

        int offset =  editor.getCaretModel().getOffset();

        int col = editor.getCaretModel().getLogicalPosition().column;
        int line = editor.getCaretModel().getLogicalPosition().line;
        final Document document = editor.getDocument();
        int lineStartOffset = document.getLineStartOffset(line);
        CharSequence docText = document.getCharsSequence();
        String textBeforeCaret = docText.subSequence(lineStartOffset, offset).toString();
        final String afterCaret = docText.subSequence(offset, document.getLineEndOffset(line)).toString();
        if (textBeforeCaret.trim().length() > 0 && StringUtil.isEmptyOrSpaces(afterCaret) && !editor.getSelectionModel().hasSelection()) {
            EnterAction.insertNewLineAtCaret(editor);
            PsiDocumentManager.getInstance(project).commitDocument(document);
            offset = editor.getCaretModel().getOffset();
            col = editor.getCaretModel().getLogicalPosition().column;
            line = editor.getCaretModel().getLogicalPosition().line;
        }

        editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(0, 0));

        List<? extends GenerationInfo> newMembers;
        try{
            List<GenerationInfo> prototypes = new ArrayList<GenerationInfo>();

            //create method
            PsiManager manager = aClass.getManager();
            JVMElementFactory factory = JVMElementFactories.requireFactory(aClass.getLanguage(), aClass.getProject());
            CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(manager.getProject());


            PsiMethod method = factory.createMethod(methodName.replaceAll(":", ""), PsiType.VOID);

            PsiParameter field = factory.createParameter("sender", new PsiPrimitiveType("NSObject", new PsiAnnotation[]{}));
            method.getParameterList().add(field);
            methodName = methodName.substring(methodName.length() - 1).equals(":") ? methodName : methodName + ":";
            method.getModifierList().addAnnotation(String.format("%s(\"%s\")", SdkConstants.SELECTOR_ANNOTATION_PREFIX, methodName));
            method.getModifierList().addAnnotation("IBAction");

            prototypes.add(new PsiGenerationInfo(method));
            newMembers = GenerateMembersUtil.insertMembersAtOffset(aClass.getContainingFile(), offset, prototypes);
        }
        catch(IncorrectOperationException e){
            return;
        }

        editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(line, col));

        if (newMembers.isEmpty()) {
            return;
        }
        else {
            final List<PsiElement> elements = new ArrayList<PsiElement>();
            for (GenerationInfo member : newMembers) {
                if (!(member instanceof TemplateGenerationInfo)) {
                    final PsiMember psiMember = member.getPsiMember();
                    if (psiMember != null) {
                        elements.add(psiMember);
                    }
                }
            }
            GlobalInspectionContextBase.cleanupElements(project, null, elements.toArray(new PsiElement[elements.size()]));
        }

        final ArrayList<TemplateGenerationInfo> templates = new ArrayList<TemplateGenerationInfo>();
        for (GenerationInfo member : newMembers) {
            if (member instanceof TemplateGenerationInfo) {
                templates.add((TemplateGenerationInfo) member);
            }
        }

        if (!templates.isEmpty()){
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
            runTemplates(project, editor, templates, 0);
        }
        else if (!newMembers.isEmpty()){
            newMembers.get(0).positionCaret(editor, false);
        }
    }

    private void runTemplates(final Project myProject, final Editor editor, final List<TemplateGenerationInfo> templates, final int index) {
        TemplateGenerationInfo info = templates.get(index);
        final Template template = info.getTemplate();

        final PsiElement element = info.getPsiMember();
        final TextRange range = element.getTextRange();
        editor.getDocument().deleteString(range.getStartOffset(), range.getEndOffset());
        int offset = range.getStartOffset();
        editor.getCaretModel().moveToOffset(offset);
        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
        TemplateManager.getInstance(myProject).startTemplate(editor, template, new TemplateEditingAdapter() {
            @Override
            public void templateFinished(Template template, boolean brokenOff) {
                if (index + 1 < templates.size()){
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            new WriteCommandAction(myProject) {
                                @Override
                                protected void run(Result result) throws Throwable {
                                    runTemplates(myProject, editor, templates, index + 1);
                                }
                            }.execute();
                        }
                    });
                }
            }
        });
    }

    private boolean isMethodUnique(PsiClass psiClass, String methodName){
        boolean isMethodUnique = true;
        methodName = methodName.substring(methodName.length() - 1).equals(":") ? methodName : methodName + ":";
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
