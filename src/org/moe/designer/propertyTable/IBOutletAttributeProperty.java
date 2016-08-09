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
import com.google.common.io.Files;
import org.moe.designer.android.dom.attrs.AttributeDefinition;
import org.moe.designer.model.RadModelBuilder;
import org.moe.designer.model.RadViewComponent;
import org.moe.designer.uipreview.ChooseClassDialog;
import org.moe.designer.utils.IBOutletUtils;
import com.intellij.codeInsight.generation.*;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInspection.ex.GlobalInspectionContextBase;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.io.fs.FileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class IBOutletAttributeProperty extends AttributeProperty {
    private String _namespace;

    public IBOutletAttributeProperty(@NotNull String name, String namespace, @NotNull AttributeDefinition definition) {
        super(name, definition);
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
                if (StringUtil.isEmpty((String)value)) {
                    XmlAttribute attribute = getAttribute(component);
                    if (attribute != null) {
                        attribute.delete();
                    }
                }
                else {
                    RadComponent root = component.getRoot();
                    if(root instanceof RadViewComponent) {
                        RadViewComponent rootView = (RadViewComponent) root;
                        XmlAttribute attribute = rootView.getTag().getAttribute("xrt:viewController");
                        if(attribute != null && isNameCorrect(value.toString())){

                            String correctValue = value.toString().replaceAll("[\":]", "");
                            String escapedValue = XmlUtils.toXmlAttributeValue(correctValue);
                            component.getTag().setAttribute(_namespace, escapedValue);
                            addStubToViewController(component, escapedValue);
                        }
                    }
                }
            }
        });
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
            XmlAttribute attribute = rootView.getTag().getAttribute("xrt:viewController");
            if(attribute != null){
                String controllerClass = attribute.getValue();
                if(module != null && controllerClass != null && !controllerClass.isEmpty()){
                    PsiClass psiClass = ChooseClassDialog.findClass(module, controllerClass);
                    if (psiClass != null && isMethodUnique(psiClass, value.toString())){
                        Document javaClass = psiClass.getContainingFile().getViewProvider().getDocument();
                        Project project = module.getProject();
                        Editor editor = EditorFactory.getInstance().createEditor(javaClass, project, psiClass.getContainingFile().getVirtualFile(), false);
                        doGenerate(component, project, editor, psiClass, new ClassMember[]{}, value.toString());

                        project.save();
                    }
                }
            }

        }
    }

    private void doGenerate(final RadViewComponent component, final Project project, final Editor editor, PsiClass aClass, ClassMember[] members, String methodName) {
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
            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            String wMethodName = String.format("get%s%s", methodName.substring(0, 1).toUpperCase(), methodName.substring(1));


            String returnType = IBOutletUtils.getReturnTypeByTagName(component.getTag().getName());


            PsiMethod method = factory.createMethodFromText(String.format("public native %s %s();", returnType, wMethodName), null);
            method.getModifierList().addAnnotation(String.format("%s(\"%s\")", SdkConstants.SELECTOR_ANNOTATION_PREFIX, methodName));
            method.getModifierList().addAnnotation(SdkConstants.PROPERTY_ANNOTATION_PREFIX);

            prototypes.add(new PsiGenerationInfo(method));
            newMembers = GenerateMembersUtil.insertMembersAtOffset(aClass.getContainingFile(), offset, prototypes);
        }
        catch(IncorrectOperationException e) {
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
                if (index + 1 < templates.size()) {
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
