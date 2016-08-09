/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.moe.designer.actions;

import com.android.SdkConstants;
import com.android.resources.ResourceFolderType;
import org.moe.designer.UIDesignerPlugin;
import org.moe.designer.android.AndroidFacet;
import org.moe.designer.android.dom.layout.AndroidLayoutUtil;
import org.moe.designer.ixml.IXmlFile;
import org.moe.designer.model.ViewsMetaManager;
import org.moe.designer.rendering.LayoutPullParserFactory;
import org.moe.designer.uipreview.AndroidEditorSettings;
import org.moe.designer.utils.AndroidResourceUtil;
import com.intellij.CommonBundle;
import com.intellij.designer.model.MetaModel;
import com.intellij.history.LocalHistoryAction;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.refactoring.XmlTagInplaceRenamer;
import org.jdesktop.swingx.JXComboBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

//import com.android.tools.idea.navigator.AndroidProjectViewPane;
//import org.jetbrains.android.dom.layout.AndroidLayoutUtil;
//import org.jetbrains.android.facet.AndroidFacet;
//import org.jetbrains.android.util.AndroidBundle;

/**
 * @author Eugene.Kudelevsky
 *         <p/>
 *         Like CreateTypedResourceFileAction but prompts for a root tag
 */
public class IXmlFileCreationAction extends AnAction {
    private static final String SEGUE_TYPE_PROP = "xrt:segue_type";
    private static final String SEGUE_DESTINATION_PROP = "xrt:segue_destination";
    private String myResourcePresentableName;
    private String myLastRootComponentName;
    private ResourceFolderType myResourceType;

    public IXmlFileCreationAction() {
        myResourceType = ResourceFolderType.LAYOUT;
        myResourcePresentableName = "Layout";
    }

    public IXmlFileCreationAction(@NotNull String resourcePresentableName,
                                  @NotNull ResourceFolderType resourceFolderType) {
//        super(resourcePresentableName, resourceFolderType, false, false);
    }

    @Contract("null -> false")
    private static boolean isIOSResFolder(@Nullable VirtualFile file) {
        return file != null && file.toString().contains("layout");
    }

    //TODO: enable it when need to make resource file for ios directory only
    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();
        final VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        Module foundModule = UIDesignerPlugin.findModuleForFile(project, file);
        boolean isMoeModule = UIDesignerPlugin.isValidMoeModule(foundModule);
        boolean isIOSFolder = isIOSResFolder(file);
        e.getPresentation().setEnabled(isIOSFolder & file != null & isMoeModule);
        e.getPresentation().setVisible(isIOSFolder & file != null & isMoeModule);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final IdeView view = LangDataKeys.IDE_VIEW.getData(e.getDataContext());
        final PsiDirectory directory = view.getOrChooseDirectory();
        if (directory != null) {

//            InputValidator validator = new MyValidator(e.getProject(), directory);
            final MyDialog dialog = new MyDialog(e.getProject(), directory);
            dialog.show();
        }

//        return PsiElement.EMPTY_ARRAY;
    }

    @NotNull
//    @Override
    public List<String> getAllowedTagNames(@NotNull AndroidFacet facet) {
        assert myResourceType == ResourceFolderType.LAYOUT; // if not, must override getAllowedTagNames
        return AndroidLayoutUtil.getPossibleRoots(facet);
    }

//    @NotNull
//    @Override
//    protected PsiElement[] invokeDialog(@NotNull Project project, @NotNull DataContext dataContext) {
//        final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
//        if (view != null) {
//            // If you're in the Android View, we want to ask you not just the filename but also let you
//            // create other resource folder configurations
//            AbstractProjectViewPane pane = ProjectView.getInstance(project).getCurrentProjectViewPane();
////            if (pane instanceof AndroidProjectViewPane) {
////                return CreateResourceFileAction.getInstance().invokeDialog(project, dataContext);
////            }
//
//            final PsiDirectory directory = view.getOrChooseDirectory();
////            if (directory != null) {
////                InputValidator validator = createValidator(project, directory);
////                final AndroidFacet facet = AndroidFacet.getInstance(directory);
////                if (facet != null) {
////                    final MyDialog dialog = new MyDialog(facet, validator);
////                    dialog.show();
////                    return PsiElement.EMPTY_ARRAY;
////                }
////            }
//        }
//
//        Module module = LangDataKeys.MODULE.getData(dataContext);
//        if (module != null) {
//            final AndroidFacet facet = AndroidFacet.getInstance(module);
//            assert facet != null;
//            PsiDirectory directory = getResourceDirectory(null, module, true);
//            if (directory != null) {
//                PsiDirectory typeDirectory = directory.findSubdirectory(myResourceType.getName());
//                if (typeDirectory == null) {
//                    return PsiElement.EMPTY_ARRAY;
//                }
//                InputValidator validator = createValidator(project, typeDirectory);
//                final MyDialog dialog = new MyDialog(facet, validator);
//                dialog.show();
//            }
//        }
//        return PsiElement.EMPTY_ARRAY;
//    }

//    @NotNull
//    @Override
//    protected PsiElement[] create(String newName, PsiDirectory directory) throws Exception {
//        final String rootTag = myLastRootComponentName != null ? myLastRootComponentName : getDefaultRootTag();
//        return doCreateAndNavigate(newName, directory, rootTag, false, true);
//    }

    private enum ViewControllerType {
        VIEW_CONTROLLER("View", "com.android.sdklib.widgets.iOSViewController", null),
        NAVIGATION_CONTROLLER("Navigation View", "com.android.sdklib.widgets.iOSNavigationController", null),
        TABLE_VIEW_CONTROLLER("Table View", "com.android.sdklib.widgets.iOSTableViewController", "com.android.sdklib.widgets.iOSTableView");

        private String _type;
        private String _rootTag;
        private String _subTag;

        ViewControllerType(String type, String rootTag, String subtag) {
            _type = type;
            _rootTag = rootTag;
            _subTag = subtag;
        }

        public String getType() {
            return _type;
        }

        public String getRootTag() {
            return _rootTag;
        }

        public String getSubTag() {
            return _subTag;
        }

        @Override
        public String toString() {
            return _type;
        }
    }

    public class MyDialog extends DialogWrapper {
        private final InputValidator myValidator;

        private LanguageTextField myFileNameField;
        private JComboBox<ViewControllerType> myRootElementField;
        private JPanel myPanel;
        private JPanel myRootElementFieldWrapper;
        private JBLabel myRootElementLabel;
        private JBLabel myNavigationControllerLabel;
        private JComboBox<String> myNavigationControllerField;
        private JBLabel myRootViewLabel;
        private JCheckBox myRootViewCheckbox;
        private Project myProject;
        private PsiDirectory myDirectory;

        protected MyDialog(Project project, PsiDirectory dir) {
            super(project);
            myProject = project;
            myDirectory = dir;
            myValidator = null;
            myRootElementFieldWrapper = new JPanel(new BorderLayout());
            myRootElementFieldWrapper.setLayout(new BorderLayout());
            myRootElementLabel = new JBLabel("Root Element");
            myFileNameField = new TextFieldWithAutoCompletion<String>(
                    myProject, new TextFieldWithAutoCompletion.StringsCompletionProvider(null, null), false, null);
            setTitle(myResourcePresentableName);

            myRootElementField = new JXComboBox();
            for (ViewControllerType type : ViewControllerType.values()) {
                myRootElementField.addItem(type);
            }
            myRootElementField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ViewControllerType type = (ViewControllerType) ((JXComboBox) e.getSource()).getSelectedItem();
                    if (type.equals(ViewControllerType.NAVIGATION_CONTROLLER)) {
                        myNavigationControllerField.setEnabled(false);
                        myRootViewCheckbox.setEnabled(false);
                    } else {
                        myRootViewCheckbox.setEnabled(true);
                        if (myRootViewCheckbox.isSelected()) {
                            myNavigationControllerField.setEnabled(true);
                        }
                    }
                }
            });
            myRootElementFieldWrapper.add(myRootElementField, BorderLayout.CENTER);
            myRootElementLabel.setLabelFor(myRootElementField);


            myNavigationControllerLabel = new JBLabel("Navigation View");
            myNavigationControllerField = new JXComboBox();
            for (PsiElement child : myDirectory.getChildren()) {
                if (child instanceof IXmlFile) {
                    XmlTag rootTag = ((IXmlFile) child).getRootTag();
                    if (rootTag.getName().equals("com.android.sdklib.widgets.iOSNavigationController")) {
                        myNavigationControllerField.addItem(((IXmlFile) child).getName());
                    }
                }
            }
            myNavigationControllerLabel.setLabelFor(myNavigationControllerField);
            myNavigationControllerField.setEnabled(false);

            myRootViewLabel = new JBLabel("Is Root View");
            myRootViewCheckbox = new JCheckBox();
            myRootViewCheckbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (((JCheckBox) e.getSource()).isSelected() && !myRootElementField.getSelectedItem().equals(ViewControllerType.NAVIGATION_CONTROLLER)) {
                        myNavigationControllerField.setEnabled(true);
                    } else if (!((JCheckBox) e.getSource()).isSelected()) {
                        myNavigationControllerField.setEnabled(false);
                    }
                }
            });
            myRootViewLabel.setLabelFor(myRootViewCheckbox);

            myFileNameField.setSize(myRootElementField.getSize());
            myPanel = new JPanel(new GridLayout(4, 4));
            myPanel.add(new JLabel("New file:"));
            myPanel.add(myFileNameField);
            myPanel.add(new JBLabel("View Controller Type"));
            myPanel.add(myRootElementField);

            myPanel.add(myRootViewLabel);
            myPanel.add(myRootViewCheckbox);

            myPanel.add(myNavigationControllerLabel);
            myPanel.add(myNavigationControllerField);


            init();

//            myFileNameField.getDocument().addDocumentListener(new DocumentAdapter() {
//                @Override
//                public void textChanged(DocumentEvent event) {
//                    final String text = myFileNameField.getText().trim();
//                    if (myValidator instanceof InputValidatorEx) {
//                        setErrorText(((InputValidatorEx) myValidator).getErrorText(text));
//                    }
//                }
//            });
        }

        private List<String> getSortedAllowedTagNames() {
            return new ArrayList<String>() {{
                add("LinearLayout");
                add("RelativeLayout");
            }};
        }


        @Override
        protected JComponent createCenterPanel() {
            return myPanel;
        }

        @Override
        public JComponent getPreferredFocusedComponent() {
            return myFileNameField;
        }

        @Override
        protected void doOKAction() {
            final String fileName = myFileNameField.getText().trim();
//            myLastRootComponentName = myRootElementField.getText().trim();

            if (fileName.length() == 0) {
//                Messages.showErrorDialog(myPanel, AndroidBundle.message("file.name.not.specified.error"), CommonBundle.getErrorTitle());
                return;
            }

            ViewControllerType type = (ViewControllerType) myRootElementField.getSelectedItem();
            myLastRootComponentName = type.getRootTag();

            String navigationController = (String) myNavigationControllerField.getSelectedItem();

            PsiElement[] tmp = tryCreate(fileName, type, navigationController);
            if (myValidator == null || tmp.length > 0 ||
                    myValidator.checkInput(fileName) && myValidator.canClose(fileName)) {
                super.doOKAction();
            }
        }

        public PsiElement[] tryCreate(@NotNull final String inputString, @NotNull final ViewControllerType type, final String navigationController) {
            if (inputString.length() == 0) {
                Messages.showMessageDialog(myProject, IdeBundle.message("error.name.should.be.specified"), CommonBundle.getErrorTitle(),
                        Messages.getErrorIcon());
                return PsiElement.EMPTY_ARRAY;
            }

            final Exception[] exception = new Exception[1];
            final SmartPsiElementPointer[][] myCreatedElements = {null};

            final String commandName = "";//getActionName(inputString);
            new WriteCommandAction(myProject, commandName) {
                @Override
                protected void run(Result result) throws Throwable {
                    LocalHistoryAction action = LocalHistoryAction.NULL;
                    try {
                        PsiElement[] psiElements = doCreateAndNavigate(inputString, myDirectory, myLastRootComponentName, type, false, true);
                        //set segue destination for controller
                        if (!type.equals(ViewControllerType.NAVIGATION_CONTROLLER) && navigationController != null && myRootViewCheckbox.isSelected()) {
                            PsiFile file = myDirectory.findFile(navigationController);
                            if (file instanceof IXmlFile) {
                                XmlTag rootTag = ((IXmlFile) file).getRootTag();
                                if (rootTag != null) {
                                    SegueDestinationController controller = SegueDestinationController.getInstance(file.getProject());
                                    String oldValue = "";
                                    XmlAttribute attribute = rootTag.getAttribute("xrt:segue_destination");
                                    if (attribute != null) {
                                        oldValue = attribute.getValue();
                                    }
                                    controller.updateNavigationController(file.getVirtualFile(), oldValue, inputString + ".ixml");
                                    rootTag.setAttribute(SEGUE_DESTINATION_PROP, inputString + ".ixml");
                                }
                            }
                        }
                        myCreatedElements[0] = new SmartPsiElementPointer[psiElements.length];
                        SmartPointerManager manager = SmartPointerManager.getInstance(myProject);
                        for (int i = 0; i < myCreatedElements[0].length; i++) {
                            myCreatedElements[0][i] = manager.createSmartPsiElementPointer(psiElements[i]);
                        }
                    } catch (Exception ex) {
                        exception[0] = ex;
                    } finally {
//                        action.finish();
                    }
                }

                @Override
                protected UndoConfirmationPolicy getUndoConfirmationPolicy() {
                    return UndoConfirmationPolicy.REQUEST_CONFIRMATION;
                }
            }.execute();

            if (exception[0] != null) {
//                LOG.info(exception[0]);
                String errorMessage = CreateElementActionBase.filterMessage(exception[0].getMessage());
                if (errorMessage == null || errorMessage.length() == 0) {
                    errorMessage = exception[0].toString();
                }
                Messages.showMessageDialog(myProject, errorMessage, "", Messages.getErrorIcon());
                return PsiElement.EMPTY_ARRAY;
            }

            List<PsiElement> result = new SmartList<PsiElement>();
            for (final SmartPsiElementPointer pointer : myCreatedElements[0]) {
                ContainerUtil.addIfNotNull(pointer.getElement(), result);
            }
            return PsiUtilCore.toPsiElementArray(result);
        }

        public PsiElement[] doCreateAndNavigate(String newName, PsiDirectory directory, String rootTagName, ViewControllerType type, boolean chooseTagName, boolean navigate)
                throws Exception {
            IXmlFile file = AndroidResourceUtil
                    .createIXmlFileResource(newName, directory, rootTagName, myResourceType.getName(), false);
            processFile(file, directory, newName, type, chooseTagName, navigate);

            IXmlFile rootViewFile = null;
            if (type.equals(ViewControllerType.NAVIGATION_CONTROLLER)) {
                rootViewFile = AndroidResourceUtil
                        .createIXmlFileResource(newName + "_rootView", directory, ViewControllerType.TABLE_VIEW_CONTROLLER.getRootTag(), myResourceType.getName(), false);
            }

            //process created files
            if (rootViewFile != null) {
                processFile(rootViewFile, directory, newName, ViewControllerType.TABLE_VIEW_CONTROLLER, chooseTagName, navigate);

                //add segue property for navigation controller - file.ixml
                XmlDocument document = file.getDocument();
                if (document != null) {
                    XmlTag rootTag = document.getRootTag();
                    if (rootTag != null) {
                        //todo set special property
                        rootTag.setAttribute(SEGUE_TYPE_PROP, "relationship");
                        rootTag.setAttribute(SEGUE_DESTINATION_PROP, rootViewFile.getName());
                        SegueDestinationController controller = SegueDestinationController.getInstance(file.getProject());
                        controller.updateNavigationController(file.getVirtualFile(), "", rootViewFile.getName());
                    }
                }
            }
            return new PsiElement[]{file};
        }

        protected void doNavigate(IXmlFile file) {
            if (file.isValid() && LayoutPullParserFactory.isSupported(file)) {
                VirtualFile virtualFile = file.getVirtualFile();
                if (virtualFile != null && virtualFile.isValid()) {
                    if (AndroidEditorSettings.getInstance().getGlobalState().isPreferXmlEditor()) {
                        new OpenFileDescriptor(file.getProject(), virtualFile, 0).navigate(true);
                    } else {
                        new OpenFileDescriptor(file.getProject(), virtualFile).navigate(true);
                    }
                }
            } else {
                PsiNavigateUtil.navigate(file);
            }
        }

        private void processFile(IXmlFile file, PsiDirectory directory, String newName, ViewControllerType type, boolean chooseTagName, boolean navigate) {
            if (navigate) {
                doNavigate(file);
            }
            if (chooseTagName) {
                XmlDocument document = file.getDocument();
                if (document != null) {
                    XmlTag rootTag = document.getRootTag();
                    if (rootTag != null) {
                        final Project project = file.getProject();
                        final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                        if (editor != null) {
                            CaretModel caretModel = editor.getCaretModel();
                            caretModel.moveToOffset(rootTag.getTextOffset() + 1);
                            XmlTagInplaceRenamer.rename(editor, rootTag);
                        }
                    }
                }
            }

            //check if first ixml - set initialViewController=true
            boolean isFirstIXML = true;
            for (PsiElement child : directory.getChildren()) {
                if (child instanceof IXmlFile) {
                    String name = ((IXmlFile) child).getName();
                    name = name.substring(0, name.lastIndexOf("."));
                    if (!name.equals(newName)) {
                        isFirstIXML = false;
                        break;
                    }
                }
            }

            XmlDocument document = file.getDocument();
            if (document != null) {
                XmlTag rootTag = document.getRootTag();
                if (rootTag != null) {
                    if (isFirstIXML) {
                        rootTag.setAttribute(String.format("%s:%s", SdkConstants.XRT_NAMESPACE, "initialViewController"), "true");
                    }
                    rootTag.setAttribute("android:orientation", "vertical");
                }
            }

            if (type != null) {
                String subTag = type.getSubTag();
                if (subTag != null) {
                    ViewsMetaManager metaManager = ViewsMetaManager.getInstance(myProject);
                    MetaModel model = metaManager.getModelByTag(subTag);

                    XmlElementFactory factory = XmlElementFactory.getInstance(file.getProject());
                    XmlTag tag = factory.createTagFromText(model.getCreation());
                    file.getRootTag().addSubTag(tag, true);
                }
            }
        }

        private void setAttributeForFile(IXmlFile file, String attribute, String value) {
            XmlDocument document = file.getDocument();
            if (document != null) {
                XmlTag rootTag = document.getRootTag();
                if (rootTag != null) {
                    rootTag.setAttribute(attribute, value);
                }
            }
        }
    }
}
