/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package org.moe.designer.propertyTable.editors;

import com.android.resources.ResourceType;
//import com.intellij.android.designer.model.RadModelBuilder;
//import com.intellij.android.designer.propertyTable.renderers.EventHandlerEditorRenderer;
import org.moe.designer.android.augment.AndroidPsiElementFinder;
import org.moe.designer.android.dom.attrs.AttributeFormat;
import org.moe.designer.android.dom.converters.OnClickConverter;
import org.moe.designer.model.RadModelBuilder;
import org.moe.designer.model.RadViewComponent;
import org.moe.designer.propertyTable.renderers.EventHandlerEditorRenderer;
import org.moe.designer.uipreview.ChooseClassDialog;
import org.moe.designer.utils.IOSPsiUtils;
import com.intellij.designer.model.PropertiesContainer;
import com.intellij.designer.model.PropertyContext;
import com.intellij.designer.model.RadComponent;
import com.intellij.designer.model.RadPropertyContext;
import com.intellij.designer.propertyTable.InplaceContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
//import org.jetbrains.android.dom.attrs.AttributeFormat;
//import org.jetbrains.android.dom.converters.OnClickConverter;
//import org.jetbrains.android.uipreview.ChooseClassDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/**
 * @author Alexander Lobas
 */
public class EventHandlerEditor extends ResourceEditor {
  private static final ResourceType[] TYPES = {ResourceType.STRING};
  private static final Set<AttributeFormat> FORMATS = EnumSet.of(AttributeFormat.String, AttributeFormat.Enum);
  private boolean isHandler = false;

  public EventHandlerEditor() {
    super(TYPES, FORMATS, ArrayUtil.EMPTY_STRING_ARRAY);
    getCombo().setRenderer(new EventHandlerEditorRenderer());
  }

  public EventHandlerEditor(Set<AttributeFormat> formats) {
    super(TYPES, FORMATS, ArrayUtil.EMPTY_STRING_ARRAY);
    if(formats.contains(AttributeFormat.Handler)){
      myEditor.setButtonVisible(false);
      isHandler = true;
    }

    getCombo().setRenderer(new EventHandlerEditorRenderer());
  }

  @Override
  public Object getValue() {
    Object item = getCombo().getSelectedItem();
    if (item instanceof PsiMethodWrapper) {
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
      for (PsiClass psiClass : ChooseClassDialog.findInheritors(module, "android.app.Activity", true)) {
        for (PsiMethod method : psiClass.getMethods()) {
          if (OnClickConverter.CONVERTER_FOR_LAYOUT.checkSignature(method) && names.add(method.getName())) {
            model.addElement(new PsiMethodWrapper(method));
          }
        }
      }
    }

    combo.setSelectedItem(value);
    return myEditor;
  }


  private JComboBox getCombo() {
    return (JComboBox)myEditor.getChildComponent();
  }

  public static final class PsiMethodWrapper {
    private final PsiMethod myMethod;

    public PsiMethodWrapper(PsiMethod method) {
      myMethod = method;
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
      return myMethod.getName();
    }
  }
}