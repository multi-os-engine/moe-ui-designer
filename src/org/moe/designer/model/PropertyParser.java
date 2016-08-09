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
package org.moe.designer.model;
//
//import com.android.ide.common.rendering.LayoutLibrary;
//import com.android.ide.common.rendering.api.ViewInfo;
//import com.android.sdklib.IAndroidTarget;
import com.android.SdkConstants;
import com.android.ide.common.rendering.LayoutLibrary;
import com.android.ide.common.rendering.api.ViewInfo;
import com.android.sdklib.IAndroidTarget;
import org.moe.designer.android.dom.attrs.*;
import org.moe.designer.android.sdk.AndroidPlatform;
import org.moe.designer.android.sdk.AndroidTargetData;
import org.moe.designer.propertyTable.*;
import org.moe.designer.propertyTable.renderers.XRTAttributeProperty;
import org.moe.designer.propertyTable.renderers.XRTStyleAttributeProperty;
import org.moe.designer.rendering.RenderResult;
import org.moe.designer.rendering.RenderService;
import org.moe.designer.uipreview.ProjectClassLoader;
import com.intellij.designer.model.*;
import com.intellij.designer.propertyTable.PropertyTable;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Alexander Lobas
 */
@SuppressWarnings("unchecked")
public class PropertyParser {
  public static final String KEY = "PROPERTY_PARSER";

  private static final String[] DEFAULT_LAYOUT_PARAMS = {"ViewGroup_Layout"};
  private static final String LAYOUT_PREFIX = "layout_";
  private static final String LAYOUT_MARGIN_PREFIX = "layout_margin";

  private MetaManager myMetaManager;
  private AttributeDefinitions myDefinitions;
  private ProjectClassLoader myClassLoader;
  private Map<String, List<Property>> myCachedProperties;

  public PropertyParser(@NotNull RenderResult result)  {
    assert result.getSession() != null;
    assert result.getSession().getResult().isSuccess();
    RenderService renderService = result.getRenderService();
    assert renderService != null;
    IAndroidTarget target = renderService.getConfiguration().getTarget();
    assert target != null;
    Module module = renderService.getModule();

    myMetaManager = ViewsMetaManager.getInstance(module.getProject());
    myCachedProperties = myMetaManager.getCache(target.hashString());
    if (myCachedProperties == null) {
      myMetaManager.setCache(target.hashString(), myCachedProperties = new HashMap<String, List<Property>>());
    }

    AndroidPlatform androidPlatform = AndroidPlatform.getInstance(module);
    assert androidPlatform != null;
    AndroidTargetData targetData = androidPlatform.getSdkData().getTargetData(target);
    myDefinitions = targetData.getAttrDefs(module.getProject());

    LayoutLibrary library = renderService.getLayoutLib();
    myClassLoader = ProjectClassLoader.get(library, module);
  }

  public void load(RadViewComponent component) throws Exception {
    MetaModel model = component.getMetaModelForProperties();
    String target = model.getTarget();
    if (target == null) {
      ViewInfo info = component.getViewInfo();
      if (info == null) {
        component.setProperties(Collections.<Property>emptyList());
      }
      else {
        Class<?> componentClass = configureClass(myClassLoader.loadClass(info.getClassName()));
        component.setProperties(loadWidgetProperties(componentClass, model));
      }
    }
    else {
      component.setProperties(loadWidgetProperties(myClassLoader.loadClass(target), model));
    }
    component.sortWithPriorityExceptions(SdkConstants.PRIORITY_PROPERTIES.getNames());

    RadComponent parent = component.getParent();
    if (parent != null) {
      String[] layoutParams = null;
      RadLayout layout = parent.getLayout();

      if (layout instanceof RadViewLayoutWithData) {
        layoutParams = ((RadViewLayoutWithData)layout).getLayoutParams();
      }
      else if (parent == parent.getRoot()) {
        layoutParams = DEFAULT_LAYOUT_PARAMS;
      }

      if (layoutParams != null) {
        MetaModel[] models = new MetaModel[layoutParams.length];
        models[0] = parent.getMetaModelForProperties();

        for (int i = 1; i < layoutParams.length; i++) {
          if (models[i - 1] == null) {
            break;
          }
          String extendTarget = models[i - 1].getTarget();
          if (extendTarget == null) {
            break;
          }

          Class<?> superClass = myClassLoader.loadClass(extendTarget).getSuperclass();
          if (superClass != null) {
            superClass = configureClass(superClass);
            models[i] = myMetaManager.getModelByTarget(superClass.getName());
          }
        }

        List<Property> properties = loadLayoutProperties(component, layoutParams, 0, models);
        if (!properties.isEmpty()) {
          properties = new ArrayList<Property>(properties);
          properties.addAll(component.getProperties());
          component.setProperties(properties);
        }
      }
    }

  }

  private List<Property> loadWidgetProperties(Class<?> componentClass, @Nullable MetaModel model) throws Exception {
    String component = componentClass.getSimpleName();

    List<Property> properties = myCachedProperties.get(component);

    if (properties == null) {
      properties = new ArrayList<Property>();
      myCachedProperties.put(component, properties);

      if ("View".equals(component)) {
        properties.add(new StyleProperty());
      }

      StyleableDefinition definitions = myDefinitions.getStyleableByName(component);
      if (definitions != null) {
        boolean padding = false;
        boolean segue = false;

        for (AttributeDefinition definition : definitions.getAttributes()) {
          String name = definition.getName();
          Set<AttributeFormat> formats = definition.getFormats();
          Property property = null;

          if ("padding".equals(name) && "View".equals(component)) {
            padding = true;
          }

          if("segue".equals(name)){
            segue = true;
          }
          else if("orientation".equals(name)){
            property = new OrientationAttributeProperty(name, definition);
          }
          else if("segue_type".equals(name) || "segue_destination".equals(name) || "segue_identifier".equals(name)){
            if("segue_type".equals(name) && component.equals("iOSNavigationController")){
              AttributeDefinition tmp = myDefinitions.getAttrDefByName("segue_type_nav_control");
              AttributeDefinition newDef = new AttributeDefinition(definition.getName(), definition.getFormats());
              newDef.addValue(tmp.getValues()[0]);
              property = new SegueAttributeProperty(name, SdkConstants.XRT_NAMESPACE, newDef);
            }
            else{
              property = new SegueAttributeProperty(name, SdkConstants.XRT_NAMESPACE, definition);
            }
          }
          else if("events".equals(name)){
            property = new CompoundDimensionProperty("events");
            List<Property> children = property.getChildren(null);
            String[] values = definition.getValues();
            for(String value : values){
              AttributeDefinition attrDef = new AttributeDefinition(definition.getInternalName(value), definition.getFormats());
              attrDef.addValue(value);
              IBActionAttributeProperty childrenProp = new IBActionAttributeProperty(definition.getInternalName(value), SdkConstants.NAMESPACE_IBACTION, attrDef);
              children.add(childrenProp.createForNewIBPresentation(property, value, SdkConstants.NAMESPACE_IBACTION));
            }
            property.setImportant(true);
          }
          else if ("iboutlet".equals(name)){
            property = new IBOutletAttributeProperty(name, SdkConstants.NAMESPACE_IBOUTLET, definition);
            property.setImportant(true);
          }
          else if (formats.contains(AttributeFormat.Flag)) {
            property = new FlagProperty(name, definition);
          }
          else if("initialViewController".equals(name)){
            property = new UniqueViewComponentProperty(name, SdkConstants.XRT_NAMESPACE, definition);
            property.setImportant(true);
          }
          else if(definition.getClass() == XRTAttributeDifinition.class && "style".equals(name)){
            property = new XRTStyleAttributeProperty(name, SdkConstants.XRT_NAMESPACE, definition);
            property.setImportant(true);
          }
          else if(definition.getClass() == XRTAttributeDifinition.class){
            property = new XRTAttributeProperty(name, SdkConstants.XRT_NAMESPACE, definition);
            property.setImportant(true);
          }
          else {
            if ("id".equals(name) && "View".equals(component)) {
              property = new IdProperty(name, definition);
            }
            else {
              property = new AttributeProperty(name, definition);
            }
          }

          if(property != null){
            if (model != null) {
              model.decorate(property, name);
            }
            properties.add(property);
          }
        }


        if (padding) {
          CompoundDimensionProperty paddingProperty = new CompoundDimensionProperty("padding");
          moveProperties(properties, paddingProperty,
                  "padding", "all",
                  "paddingLeft", "left",
                  "paddingTop", "top",
                  "paddingRight", "right",
                  "paddingBottom", "bottom");
          if (model != null) {
            paddingProperty.decorate(model);
          }
          properties.add(paddingProperty);
        }

        if (segue) {
          CompoundDimensionProperty segueProperty = new CompoundDimensionProperty("segue");
          moveProperties(properties, segueProperty,
                  "segue_type", "Type",
                  "segue_destination", "Destination",
                  "segue_identifier", "Identifier");
          if (model != null) {
            segueProperty.decorate(model);
          }
          segueProperty.setImportant(true);
          properties.add(segueProperty);
        }

      }

      //get all properties for fake intefaces
      List<Property> interfaceProps;
      for(Class<?> parentInterface : componentClass.getInterfaces()){
        interfaceProps = loadWidgetProperties(parentInterface, null);

        for (Property superProperty : interfaceProps) {
          if (PropertyTable.findProperty(properties, superProperty) == -1) {
            if (model == null) {
              properties.add(superProperty);
            }
            else {
              properties.add(model.decorateWithOverride(superProperty));
            }
          }
        }
      }

      Class<?> superComponentClass = componentClass.getSuperclass();
      if (superComponentClass != null && isClassDescedantOfIOSBase(superComponentClass)) {
        superComponentClass = configureClass(superComponentClass);
        MetaModel superModel = myMetaManager.getModelByTarget(superComponentClass.getName());

        if (model != null && superModel != null && model.getInplaceProperties().isEmpty()) {
          model.setInplaceProperties(superModel.getInplaceProperties());
        }

        List<Property> superProperties = loadWidgetProperties(superComponentClass, superModel);
        for (Property superProperty : superProperties) {
          if (PropertyTable.findProperty(properties, superProperty) == -1) {
            if (model == null) {
              properties.add(superProperty);
            }
            else {
              properties.add(model.decorateWithOverride(superProperty));
            }
          }
        }
      }

      if (!properties.isEmpty()) {
        Collections.sort(properties, new Comparator<Property>() {
          @Override
          public int compare(Property p1, Property p2) {
            return p1.getName().compareTo(p2.getName());
          }
        });
        if (model != null) {
          for (String topName : model.getTopProperties()) {
            PropertyTable.moveProperty(properties, topName, properties, 0);
          }
        }


//        PropertyTable.moveProperty(properties, "layout:gravity", properties, 0);
      }
    }

    return properties;
  }

  private boolean isClassDescedantOfIOSBase(Class element){
    if(element != null){
      for(Class<?> interClass : element.getInterfaces()){
        if(interClass.getSimpleName().equals("iOSView")){
          return true;
        }
      }
      return isClassDescedantOfIOSBase(element.getSuperclass());
    }
    return false;
  }

  private Class<?> configureClass(Class<?> viewClass) throws Exception {
    if (viewClass.getName().equals("com.android.layoutlib.bridge.MockView")) {
      return myClassLoader.loadClass("android.view.View");
    }
    return viewClass;
  }

  private List<Property> loadLayoutProperties(RadViewComponent viewComponent, String[] components, int index, MetaModel[] models) throws Exception {
    String component = components[index];
    MetaModel model = models[index];

    List<Property> properties = myCachedProperties.get(component);

    if (properties == null) {
      properties = new ArrayList<Property>();
      myCachedProperties.put(component, properties);

      StyleableDefinition definitions = myDefinitions.getStyleableByName(component);
      if (definitions != null) {
        boolean margin = false;
        boolean segue = false;

        for (AttributeDefinition definition : definitions.getAttributes()) {
          String name = definition.getName();
          boolean important = true;
          Set<AttributeFormat> formats = definition.getFormats();
          Property property = null;

          if (name.startsWith(LAYOUT_MARGIN_PREFIX)) {
            name = name.substring(LAYOUT_PREFIX.length());
            important = false;
          }
          else if (name.startsWith(LAYOUT_PREFIX)) {
            name = "layout:" + name.substring(LAYOUT_PREFIX.length());
          }

          if ("margin".equals(name) && "ViewGroup_MarginLayout".equals(component)) {
            margin = true;
          }

          if("segue".equals(name)){
            segue = true;
          }
          else if("orientation".equals(name)){
            property = new OrientationAttributeProperty(name, definition);
          }
          else if("segue_type".equals(name) || "segue_destination".equals(name) || "segue_identifier".equals(name)){
            property = new SegueAttributeProperty(name, SdkConstants.XRT_NAMESPACE, definition);
          }
          else if("events".equals(name)){
            property = new CompoundDimensionProperty("events");
            List<Property> children = property.getChildren(null);
            String[] values = definition.getValues();
            for(String value : values){
              AttributeDefinition attrDef = new AttributeDefinition(definition.getInternalName(value), definition.getFormats());
              attrDef.addValue(value);
              IBActionAttributeProperty childrenProp = new IBActionAttributeProperty(definition.getInternalName(value), SdkConstants.NAMESPACE_IBACTION, attrDef);
              children.add(childrenProp.createForNewIBPresentation(property, value, SdkConstants.NAMESPACE_IBACTION));
            }
            property.setImportant(true);
          }
          else if("iboutlet".equals(name)){
            property = new IBOutletAttributeProperty(name, SdkConstants.NAMESPACE_IBOUTLET, definition);
            property.setImportant(true);
          }
          else if("initialViewController".equals(name)){
            property = new UniqueViewComponentProperty(name, SdkConstants.XRT_NAMESPACE, definition);
            property.setImportant(true);
          }
          else if (("layout:width".equals(name) || "layout:height".equals(name))) {
            property = new AttributePropertyWithDefault(name, definition, "wrap_content");
          }
          else if ("layout:weight".equals(name)){
            property = null;
          }
          else if (formats.contains(AttributeFormat.Flag)) {
            if ("layout:gravity".equals(name)) {
              Set<AttributeFormat> formatSet = new HashSet<AttributeFormat>();
              formatSet.add(AttributeFormat.Enum);
              AttributeDefinition attrDef = new AttributeDefinition(definition.getName(), formatSet);
              attrDef.addValue("top");
              attrDef.addValue("bottom");
              attrDef.addValue("left");
              attrDef.addValue("right");
              attrDef.addValue("center");

              property = new AttributeProperty(name, attrDef);
//              property = new GravityProperty(name, definition); //todo do not remove, initial code - correct
            }
            else if(definition.getClass() == XRTAttributeDifinition.class){
              property = new XRTAttributeProperty(name, SdkConstants.XRT_NAMESPACE, definition);
              property.setImportant(true);
            }
            else {
              property = new FlagProperty(name, definition);
            }
          }
          else {
            property = new AttributeProperty(name, definition);
          }

          if(property != null){
            if (model != null) {
              model.decorate(property, name);
            }
            property.setImportant(important);
            properties.add(property);
          }
        }

        if (margin) {
          CompoundDimensionProperty marginProperty = new CompoundDimensionProperty("layout:margin");
          moveProperties(properties, marginProperty,
                         "margin", "all",
                         "marginLeft", "left",
                         "marginTop", "top",
                         "marginRight", "right",
                         "marginBottom", "bottom",
                         "marginStart", "start",
                         "marginEnd", "end");
          if (model != null) {
            marginProperty.decorate(model);
          }
          marginProperty.setImportant(true);
          properties.add(marginProperty);
        }

        if (segue) {
          CompoundDimensionProperty segueProperty = new CompoundDimensionProperty("segue");
          moveProperties(properties, segueProperty,
                  "segue_type", "Type",
                  "segue_destination", "Destination",
                  "segue_identifier", "Identifier");
          if (model != null) {
            segueProperty.decorate(model);
          }
          segueProperty.setImportant(true);
          properties.add(segueProperty);
        }
      }

      if (++index < components.length) {
        for (Property property : loadLayoutProperties(viewComponent, components, index, models)) {
          if (PropertyTable.findProperty(properties, property) == -1) {
            if (model == null) {
              properties.add(property);
            }
            else {
              property = model.decorateWithOverride(property);
              properties.add(property);
            }
          }
        }
      }

      if (!properties.isEmpty()) {
        Collections.sort(properties, new Comparator<Property>() {
          @Override
          public int compare(Property p1, Property p2) {
            return p1.getName().compareTo(p2.getName());
          }
        });

        PropertyTable.moveProperty(properties, "layout:margin", properties, 0);
//        PropertyTable.moveProperty(properties, "layout:gravity", properties, 0); //todo do not remove
        PropertyTable.moveProperty(properties, "layout:height", properties, 0);
        PropertyTable.moveProperty(properties, "layout:width", properties, 0);
      }

      if (model != null) {
        Class<RadLayout> layout = model.getLayout();
        if (layout != null) {
          layout.newInstance().configureProperties(properties);
        }
      }
    }

    return properties;
  }

  public static void moveProperties(List<Property> source, Property destination, String... names) {
    List<Property> children = destination.getChildren(null);
    for (int i = 0; i < names.length; i += 2) {
      Property property = PropertyTable.extractProperty(source, names[i]);
      if (property != null) {
        children.add(property.createForNewPresentation(destination, names[i + 1]));
      }
    }
  }

//  public boolean isAssignableFrom(MetaModel base, MetaModel test) {
//    try {
//      Class<?> baseClass = myClassLoader.loadClass(base.getTarget());
//      Class<?> testClass = myClassLoader.loadClass(test.getTarget());
//      return baseClass.isAssignableFrom(testClass);
//    }
//    catch (Throwable ignored) {
//    }
//    return false;
//  }
}