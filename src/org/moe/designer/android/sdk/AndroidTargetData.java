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

package org.moe.designer.android.sdk;

import com.android.SdkConstants;
import com.android.ide.common.rendering.LayoutLibrary;
import com.android.ide.common.resources.FrameworkResources;
import com.android.resources.ResourceType;
import com.android.sdklib.IAndroidTarget;
//import com.android.tools.idea.AndroidPsiUtils;
//import com.android.tools.idea.rendering.multi.CompatibilityRenderTarget;
import org.moe.designer.android.dom.attrs.AttributeDefinitions;
import org.moe.designer.android.dom.attrs.AttributeDefinitionsImpl;
import org.moe.designer.android.resourceManagers.FilteredAttributeDefinitions;
import org.moe.designer.rendering.multi.CompatibilityRenderTarget;
import org.moe.designer.uipreview.LayoutLibraryLoader;
import org.moe.designer.uipreview.RenderingException;
import org.moe.designer.utils.IOSPsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;
import com.intellij.util.xml.NanoXmlUtil;
//import org.jetbrains.android.dom.attrs.AttributeDefinitions;
//import org.jetbrains.android.dom.attrs.AttributeDefinitionsImpl;
//import org.jetbrains.android.resourceManagers.FilteredAttributeDefinitions;
//import org.jetbrains.android.uipreview.LayoutLibraryLoader;
//import org.jetbrains.android.uipreview.RenderingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * @author Eugene.Kudelevsky
 */
public class AndroidTargetData {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.android.sdk.AndroidTargetData");

  private final AndroidSdkData mySdkData;
  private final IAndroidTarget myTarget;

  private volatile AttributeDefinitionsImpl myAttrDefs;
  public volatile LayoutLibrary myLayoutLibrary;

  private final Object myPublicResourceCacheLock = new Object();
  private volatile Map<String, Set<String>> myPublicResourceCache;

  private volatile MyStaticConstantsData myStaticConstantsData;
  private FrameworkResources myFrameworkResources;

  public AndroidTargetData(@NotNull AndroidSdkData sdkData, @NotNull IAndroidTarget target) {
    mySdkData = sdkData;
    myTarget = target;
  }

  @Nullable
  public AttributeDefinitions getAttrDefs(@NotNull Project project) {
    final AttributeDefinitionsImpl attrDefs = getAttrDefsImpl(project);
    return attrDefs != null ? new PublicAttributeDefinitions(attrDefs) : null;
  }

  @Nullable
  private AttributeDefinitionsImpl getAttrDefsImpl(@NotNull final Project project) {
    final String PUBLIC_BASE = "uiProperties";
    final String PROPERTIES_FILE = "ui_property.xml";
    if (myAttrDefs == null) {
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        @Override
        public void run() {
          final String attrsPath = FileUtil.toSystemIndependentName(myTarget.getPath(IAndroidTarget.ATTRIBUTES));
          final String attrsManifestPath = FileUtil.toSystemIndependentName(myTarget.getPath(IAndroidTarget.MANIFEST_ATTRIBUTES));

          //TODO MOE
          String uiPropertyPath = IOSPsiUtils.getPathOfJarResource(PUBLIC_BASE);
          File tmpFile = new File(uiPropertyPath,PROPERTIES_FILE);
          System.out.println(AndroidTargetData.class.getCanonicalName() + " path of ui_property.xml: " + uiPropertyPath + ", file is : " + tmpFile.toString());
          File propertyXML = null;
          if (tmpFile.exists()){
            uiPropertyPath = tmpFile.toString();
            propertyXML = new File(uiPropertyPath);
          }


          if (propertyXML == null) {
             uiPropertyPath= getClass().getClassLoader().getResource("uiProperties/ui_property.xml").getFile();
          }

          final XmlFile[] files = findXmlFiles(project, attrsPath,attrsManifestPath,uiPropertyPath);

          if (files != null) {
            myAttrDefs = new AttributeDefinitionsImpl(files);
          }
        }
      });
    }
    return myAttrDefs;
  }

  @Nullable
  private Map<String, Set<String>> getPublicResourceCache() {
    synchronized (myPublicResourceCacheLock) {
      if (myPublicResourceCache == null) {
        myPublicResourceCache = parsePublicResCache();
      }
      return myPublicResourceCache;
    }
  }

  public boolean isResourcePublic(@NotNull String type, @NotNull String name) {
    final Map<String, Set<String>> publicResourceCache = getPublicResourceCache();

    if (publicResourceCache == null) {
      return false;
    }
    final Set<String> set = publicResourceCache.get(type);
    return set != null && set.contains(name);
  }

  @Nullable
  private Map<String, Set<String>> parsePublicResCache() {
    final String PUBLIC_BASE = "uiProperties";
    final String PUBLIC_FILE = "public.xml";
    final String resDirPath = myTarget.getPath(IAndroidTarget.RESOURCES);
    String publicXmlPath = resDirPath +  SdkConstants.FD_RES_VALUES + File.separator + PUBLIC_FILE;
//    String publicXmlPath = getClass().getClassLoader().getResource("uiProperties/public.xml").getFile();
    VirtualFile publicXml = LocalFileSystem.getInstance().findFileByPath(
            FileUtil.toSystemIndependentName(publicXmlPath));
    if (publicXml != null) {
      try {
        final MyPublicResourceCacheBuilder builder = new MyPublicResourceCacheBuilder();
        NanoXmlUtil.parse(publicXml.getInputStream(), builder);
        myPublicResourceCache = builder.getPublicResourceCache();

        //TODO MOE

        publicXmlPath = IOSPsiUtils.getPathOfJarResource(PUBLIC_BASE);
        File tmpFile = new File(publicXmlPath,PUBLIC_FILE);
        System.out.println(AndroidTargetData.class.getCanonicalName() + " path to public.xml: " + publicXmlPath + ", file is : " + tmpFile.toString());
        VirtualFile customPublicXml = LocalFileSystem.getInstance().findFileByPath(
                FileUtil.toSystemIndependentName(tmpFile.toString()));



        if (customPublicXml == null) {
          URL publicURL = getClass().getClassLoader().getResource(PUBLIC_BASE + "/" + PUBLIC_FILE);
          customPublicXml = LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(publicURL.getFile()));
        }
        customPublicXml.refresh(false, false);

        final MyPublicResourceCacheBuilder customBuilder = new MyPublicResourceCacheBuilder();
        NanoXmlUtil.parse(customPublicXml.getInputStream(), customBuilder);
        Map<String, Set<String>> customPublicElements = customBuilder.getPublicResourceCache();
        for(Map.Entry<String, Set<String>> e : customPublicElements.entrySet()){
          if(!myPublicResourceCache.containsKey(e.getKey())){
            myPublicResourceCache.put(e.getKey(), e.getValue());
          }
          else{
            myPublicResourceCache.get(e.getKey()).addAll(e.getValue());
          }
        }

        return myPublicResourceCache;
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
    return null;
  }

  @Nullable
  public synchronized LayoutLibrary getLayoutLibrary(@NotNull Project project) throws RenderingException, IOException {
    if (myLayoutLibrary == null) {
      if (myTarget instanceof CompatibilityRenderTarget) {
        IAndroidTarget target = ((CompatibilityRenderTarget)myTarget).getRenderTarget();
        AndroidTargetData targetData = mySdkData.getTargetData(target);
        if (targetData != this) {
          myLayoutLibrary = targetData.getLayoutLibrary(project);
          return myLayoutLibrary;
        }
      }

      final AttributeDefinitionsImpl attrDefs = getAttrDefsImpl(project);
      if (attrDefs == null) {
        return null;
      }
      myLayoutLibrary = LayoutLibraryLoader.load(myTarget, attrDefs.getEnumMap());
    }

    return myLayoutLibrary;
  }

  public void clearLayoutBitmapCache(Module module) {
    if (myLayoutLibrary != null) {
      myLayoutLibrary.clearCaches(module);
    }
  }

  @NotNull
  public IAndroidTarget getTarget() {
    return myTarget;
  }

  @Nullable
  private static XmlFile[] findXmlFiles(final Project project, final String... paths) {
    XmlFile[] xmlFiles = new XmlFile[paths.length];
    for (int i = 0; i < paths.length; i++) {
      String path = paths[i];
      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
      file.refresh(true, false);
      PsiFile psiFile = file != null ? IOSPsiUtils.getPsiFileSafely(project, file) : null;
      if (psiFile == null) {
        LOG.info("File " + path + " is not found");
        return null;
//        continue;
      }
      if (!(psiFile instanceof XmlFile)) {
        LOG.info("File " + path + "  is not an xml psiFile");
        return null;
//        continue;
      }
      xmlFiles[i] = (XmlFile)psiFile;
    }
    return xmlFiles;
  }

//  @NotNull
//  public synchronized MyStaticConstantsData getStaticConstantsData() {
//    if (myStaticConstantsData == null) {
//      myStaticConstantsData = new MyStaticConstantsData();
//    }
//    return myStaticConstantsData;
//  }

  @Nullable
  public synchronized FrameworkResources getFrameworkResources() throws IOException {
    if (myFrameworkResources == null) {
      myFrameworkResources = FrameworkResourceLoader.load(myTarget);
    }

    return myFrameworkResources;
  }

  public synchronized void resetFrameworkResources() {
    myFrameworkResources = null;
  }

  @Nullable
  public static AndroidTargetData getTargetData(@NotNull IAndroidTarget target, @NotNull Module module) {
    Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
    if (sdk == null || !(sdk.getSdkType() instanceof AndroidSdkType)) {
      return null;
    }
    AndroidSdkAdditionalData data = (AndroidSdkAdditionalData)sdk.getSdkAdditionalData();
    if (data == null) {
      return null;
    }
    AndroidPlatform platform = data.getAndroidPlatform();
    if (platform == null) {
      return null;
    }

    return platform.getSdkData().getTargetData(target);
  }

  private class PublicAttributeDefinitions extends FilteredAttributeDefinitions {
    protected PublicAttributeDefinitions(@NotNull AttributeDefinitions wrappee) {
      super(wrappee);
    }

    @Override
    protected boolean isAttributeAcceptable(@NotNull String name) {
      return isResourcePublic(ResourceType.ATTR.getName(), name);
    }
  }

  private static class MyPublicResourceCacheBuilder extends NanoXmlUtil.IXMLBuilderAdapter {
    private final Map<String, Set<String>> myResult = new HashMap<String, Set<String>>();

    private String myName;
    private String myType;

    @Override
    public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
      if ("public".equals(name) && myName != null && myType != null) {
        Set<String> set = myResult.get(myType);

        if (set == null) {
          set = new HashSet<String>();
          myResult.put(myType, set);
        }
        set.add(myName);
      }
    }

    @Override
    public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type)
      throws Exception {
      if ("name".equals(key)) {
        myName = value;
      }
      else if ("type".endsWith(key)) {
        myType = value;
      }
    }

    @Override
    public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr)
      throws Exception {
      myName = null;
      myType = null;
    }

    public Map<String, Set<String>> getPublicResourceCache() {
      return myResult;
    }
  }

  public class MyStaticConstantsData {
    private final Set<String> myActivityActions;
    private final Set<String> myServiceActions;
    private final Set<String> myReceiverActions;
    private final Set<String> myCategories;

    private MyStaticConstantsData() {
      myActivityActions = collectValues(IAndroidTarget.ACTIONS_ACTIVITY);
      myServiceActions = collectValues(IAndroidTarget.ACTIONS_SERVICE);
      myReceiverActions = collectValues(IAndroidTarget.ACTIONS_BROADCAST);
      myCategories = collectValues(IAndroidTarget.CATEGORIES);
    }

    @Nullable
    public Set<String> getActivityActions() {
      return myActivityActions;
    }

    @Nullable
    public Set<String> getServiceActions() {
      return myServiceActions;
    }

    @Nullable
    public Set<String> getReceiverActions() {
      return myReceiverActions;
    }

    @Nullable
    public Set<String> getCategories() {
      return myCategories;
    }

    @Nullable
    private Set<String> collectValues(int pathId) {
      final Set<String> result = new HashSet<String>();
      try {
        final BufferedReader reader = new BufferedReader(new FileReader(myTarget.getPath(pathId)));

        try {
          String line;

          while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.length() > 0 && !line.startsWith("#")) {
              result.add(line);
            }
          }
        }
        finally {
          reader.close();
        }
      }
      catch (IOException e) {
        return null;
      }
      return result;
    }
  }
}
