/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.moe.designer.propertyTable;

import com.android.SdkConstants;
import org.moe.designer.android.AndroidFacet;
import org.moe.designer.android.dom.attrs.AttributeDefinition;
import org.moe.designer.android.dom.attrs.StyleableDefinition;
import org.moe.designer.android.lint.SuppressLintIntentionAction;
import org.moe.designer.android.resourceManagers.LocalResourceManager;
import org.moe.designer.model.RadViewComponent;
import com.intellij.designer.model.Property;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PropertyWithNamespace extends Property<RadViewComponent> {
  public PropertyWithNamespace(@Nullable Property parent, @NotNull String name) {
    super(parent, name);
  }

  protected abstract String getAttributeName();

  public String getNamespace(RadViewComponent component, boolean createNamespaceIfNecessary) {
    String attributeName = getAttributeName();
    XmlTag tag = component.getTag();
    boolean isLayoutParam = attributeName.startsWith(SdkConstants.ATTR_LAYOUT_RESOURCE_PREFIX);
    if (isLayoutParam) {
      tag = tag.getParentTag();
    }
    if (tag != null) {
      String tagName = tag.getName();
      if (SdkConstants.VIEW_TAG.equals(tagName)) {
        tagName = tag.getAttributeValue(SdkConstants.ATTR_CLASS);
      }
      if (tagName != null && tagName.indexOf('.') != -1) {
        // Custom view; see if this attribute should be in the app namespace or in the android namespace
        AndroidFacet facet = AndroidFacet.getInstance(tag);
        if (facet != null) {
          LocalResourceManager resourceManager = facet.getLocalResourceManager();
          String styleableName = tagName.substring(tagName.lastIndexOf('.') + 1);
          if (isLayoutParam) {
            styleableName += "_Layout";
          }
          StyleableDefinition styleable = resourceManager.getAttributeDefinitions().getStyleableByName(styleableName);
          if (styleable != null) {
            for (AttributeDefinition def : styleable.getAttributes()) {
              if (def.getName().equals(attributeName)) {
                // Local namespace
                String namespace = SdkConstants.AUTO_URI;
                if (createNamespaceIfNecessary) {
                  final XmlFile file = PsiTreeUtil.getParentOfType(tag, XmlFile.class);
                  if (file != null) {
                    SuppressLintIntentionAction.ensureNamespaceImported(facet.getModule().getProject(), file, namespace);
                  }
                }
                return namespace;
              }
            }
          }
        }
      }
    }

    return SdkConstants.ANDROID_URI;
  }
}
