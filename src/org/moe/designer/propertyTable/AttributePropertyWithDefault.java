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
package org.moe.designer.propertyTable;

//import com.intellij.android.designer.model.RadViewComponent;
import com.android.utils.XmlUtils;
import org.moe.designer.android.dom.attrs.AttributeDefinition;
import org.moe.designer.model.RadViewComponent;
import org.moe.designer.utils.AndroidWidgetUtils;
import com.intellij.designer.model.Property;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.text.StringUtil;
//import org.jetbrains.android.dom.attrs.AttributeDefinition;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.android.SdkConstants.*;

/**
 * @author Alexander Lobas
 */
public class AttributePropertyWithDefault extends AttributeProperty {
  private final String myDefaultValue;

  public AttributePropertyWithDefault(@NotNull String name, @NotNull AttributeDefinition definition, @NotNull String defaultValue) {
    this(null, name, definition, defaultValue);
  }

  public AttributePropertyWithDefault(@Nullable Property parent,
                                      @NotNull String name,
                                      @NotNull AttributeDefinition definition,
                                      @NotNull String defaultValue) {
    super(parent, name, definition);
    myDefaultValue = defaultValue;
  }

  @Override
  public Property<RadViewComponent> createForNewPresentation(@Nullable Property parent, @NotNull String name) {
    return new AttributePropertyWithDefault(parent, name, myDefinition, myDefaultValue);
  }

  @Override
  public boolean isDefaultValue(@NotNull RadViewComponent component) throws Exception {
    return myDefaultValue.equals(getValue(component));
  }

  @Override
  public void setDefaultValue(@NotNull RadViewComponent component) throws Exception {
    super.setValue(component, myDefaultValue);
  }

  @Override
  public void setValue(@NotNull final RadViewComponent component, Object value) throws Exception {


    if(AndroidWidgetUtils.isResizable(component, myDefinition.getName(), getName().equals("layout:height"))){
      if (StringUtil.isEmpty((String)value)) {
        value = myDefaultValue;
      }

      final String finalValue = value.toString();
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run(){
          try{
            if(finalValue.equals("fill_parent") || finalValue.equals("match_parent")){
              if(isVertical(getFirstParentLayout((RadViewComponent) component.getParent())) && getName().equals("layout:height")){
                component.getTag().setAttribute(ATTR_LAYOUT_WEIGHT, ANDROID_URI, "1.0");
              }
              else if(!isVertical(getFirstParentLayout((RadViewComponent) component.getParent())) && getName().equals("layout:width")){
                component.getTag().setAttribute(ATTR_LAYOUT_WEIGHT, ANDROID_URI, "1.0");
              }
            }
            else{
              component.getTag().setAttribute(ATTR_LAYOUT_WEIGHT, ANDROID_URI, null);
            }
          }catch (Exception e){

          }
        }
      });
      super.setValue(component, value);
    }

  }

  protected boolean isVertical(@NotNull RadViewComponent node) {
    // Horizontal is the default, so if no value is specified it is horizontal.
    @NotNull XmlTag tag = node.getTag();
    return VALUE_VERTICAL.equals(tag.getAttributeValue(ATTR_ORIENTATION, ANDROID_URI));
  }

  private RadViewComponent getFirstParentLayout(RadViewComponent component) throws Exception {
    Class target = component.getMetaModelForProperties().getLayout();
    if(target != null && target.getName().equals("org.moe.designer.model.layout.RadLinearLayout")) {
      return component;
    }
    else{
      return getFirstParentLayout((RadViewComponent) component.getParent());
    }
  }
}