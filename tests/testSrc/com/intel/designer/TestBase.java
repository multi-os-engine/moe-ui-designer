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

package org.moe.designer;

import com.android.SdkConstants;
import com.android.ide.common.rendering.RenderSecurityManager;
import com.android.sdklib.IAndroidTarget;
import com.google.common.base.Objects;
import org.moe.designer.android.AndroidFacet;
import org.moe.designer.android.AndroidFacetConfiguration;
import org.moe.designer.android.componentTree.AndroidTreeDecorator;
import org.moe.designer.android.sdk.AndroidSdkAdditionalData;
import org.moe.designer.android.sdk.AndroidSdkData;
import org.moe.designer.android.sdk.AndroidSdkType;
import org.moe.designer.model.RadModelBuilder;
import org.moe.designer.model.RadViewComponent;
import com.intellij.designer.componentTree.AttributeWrapper;
import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import com.intellij.ui.SimpleColoredRenderer;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestBase extends LightCodeInsightFixtureTestCase {
    /** Environment variable or system property containing the full path to an SDK install */
    public static final String SDK_PATH_PROPERTY = "ADT_TEST_SDK_PATH";

    /** Environment variable or system property pointing to the directory name of the platform inside $sdk/platforms, e.g. "android-17" */
    public static final String PLATFORM_DIR_PROPERTY = "ADT_TEST_PLATFORM";


    protected JavaCodeInsightTestFixture myFixture;
    protected Module myModule;
    protected AndroidFacet myFacet;
    protected List<Module> myAdditionalModules;

    @Before
    public void init() throws Exception {
        setUp();

        String sdkPath = getTestSdkPath();

        final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getName());
        myFixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());
        final JavaModuleFixtureBuilder moduleFixtureBuilder = projectBuilder.addModule(JavaModuleFixtureBuilder.class);
        final String dirPath = myFixture.getTempDirPath() + getContentRootPath();
        final File dir = new File(dirPath);

        if (!dir.exists()) {
            Assert.assertTrue(dir.mkdirs());
        }
        tuneModule(moduleFixtureBuilder, dirPath);

        final ArrayList<MyAdditionalModuleData> modules = new ArrayList<MyAdditionalModuleData>();
//        configureAdditionalModules(projectBuilder, modules);
        System.setProperty(PlatformUtils.PLATFORM_PREFIX_KEY, "Idea");
        myFixture.setUp();
        myFixture.setTestDataPath(getTestDataPath());
        myModule = moduleFixtureBuilder.getFixture().getModule();

        createManifest();

        myFacet = addAndroidFacet(myModule, sdkPath, getPlatformDir(), true);
        myFixture.copyDirectoryToProject(getResDir(), "res");

        myAdditionalModules = new ArrayList<Module>();

        for (MyAdditionalModuleData data : modules) {
            final Module additionalModule = data.myModuleFixtureBuilder.getFixture().getModule();
            myAdditionalModules.add(additionalModule);
            final AndroidFacet facet = addAndroidFacet(additionalModule, sdkPath, getPlatformDir());
            facet.setLibraryProject(data.myLibrary);
            final String rootPath = getContentRootPath(data.myDirName);
            myFixture.copyDirectoryToProject("res", rootPath + "/res");
            myFixture.copyFileToProject(SdkConstants.FN_ANDROID_MANIFEST_XML,
                    rootPath + '/' + SdkConstants.FN_ANDROID_MANIFEST_XML);
            ModuleRootModificationUtil.addDependency(myModule, additionalModule);
        }

        if (RenderSecurityManager.RESTRICT_READS) {
            // Unit test class loader includes disk directories which security manager does not allow access to
            RenderSecurityManager.sEnabled = false;
        }
    }

    @NotNull
    protected VirtualFile getTestFile(String filename) {
        File sourceFile = new File(getTestDir(), filename);
        return myFixture.copyFileToProject(sourceFile.getPath(), "res/layout/" + filename);
    }

    protected File getTestDir() {
        return new File(FileUtil.toSystemDependentName(getTestDataPath()), "designer");
    }

    protected Project getProject() {
        return myFixture.getProject();
    }

    public String getTestDataPath() {
        return getUiDesignerHome() + "tests/testData";
    }

    public static String getUiDesignerHome() {
        String path = null;
        try {
            path = TestBase.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return path.substring(0, path.lastIndexOf("out"));
    }

    public String getDefaultTestSdkPath() {
        return getTestDataPath() + "/sdk5.1";
    }

    public static String getDefaultPlatformDir() {
        return "android-22";
//        return "android-1.5";
    }

    protected String getTestSdkPath() {
        if (requireRecentSdk()) {
            String override = getRecentSdkPath();
            if (override != null) {
                return override;
            }
            fail("This unit test requires " + SDK_PATH_PROPERTY + " and " + PLATFORM_DIR_PROPERTY + " to be defined.");
        }

        return getDefaultTestSdkPath();
    }

    @Nullable
    public static String getRecentSdkPath() {
        String override = System.getProperty(SDK_PATH_PROPERTY);
        if (override != null) {
            assertTrue("Must also define " + PLATFORM_DIR_PROPERTY, System.getProperty(PLATFORM_DIR_PROPERTY) != null);
            assertTrue(override, new File(override).exists());
            return override;
        }
        override = System.getenv(SDK_PATH_PROPERTY);
        if (override != null) {
            assertTrue("Must also define " + PLATFORM_DIR_PROPERTY, System.getenv(PLATFORM_DIR_PROPERTY) != null);
            return override;
        }

        return null;
    }

    protected String getPlatformDir() {
        if (requireRecentSdk()) {
            String override = getRecentPlatformDir();
            if (override != null) {
                return override;
            }
            fail("This unit test requires " + SDK_PATH_PROPERTY + " and " + PLATFORM_DIR_PROPERTY + " to be defined.");
        }
        return getDefaultPlatformDir();
    }

    @Nullable
    public static String getRecentPlatformDir() {
        String override = System.getProperty(PLATFORM_DIR_PROPERTY);
        if (override != null) {
            return override;
        }
        override = System.getenv(PLATFORM_DIR_PROPERTY);
        if (override != null) {
            return override;
        }
        return null;
    }

    /** Is the bundled (incomplete) SDK install adequate or do we need to find a valid install? */
    protected boolean requireRecentSdk() {
        return true;
    }

    protected String getContentRootPath() {
        return "";
    }

    public static void tuneModule(JavaModuleFixtureBuilder moduleBuilder, String moduleDirPath) {
        moduleBuilder.addContentRoot(moduleDirPath);

        //noinspection ResultOfMethodCallIgnored
        new File(moduleDirPath + "/src/").mkdir();
        moduleBuilder.addSourceRoot("src");

        //noinspection ResultOfMethodCallIgnored
        new File(moduleDirPath + "/gen/").mkdir();
        moduleBuilder.addSourceRoot("gen");
    }

    protected void createManifest() throws IOException {
        myFixture.copyFileToProject(SdkConstants.FN_ANDROID_MANIFEST_XML, SdkConstants.FN_ANDROID_MANIFEST_XML);
    }

    protected static class MyAdditionalModuleData {
        final JavaModuleFixtureBuilder myModuleFixtureBuilder;
        final String myDirName;
        final boolean myLibrary;

        private MyAdditionalModuleData(@NotNull JavaModuleFixtureBuilder moduleFixtureBuilder,
                                       @NotNull String dirName,
                                       boolean library) {
            myModuleFixtureBuilder = moduleFixtureBuilder;
            myDirName = dirName;
            myLibrary = library;
        }
    }


    public static AndroidFacet addAndroidFacet(Module module, String sdkPath, String platformDir, boolean addSdk) {
        FacetManager facetManager = FacetManager.getInstance(module);
//        AndroidFacet facet = facetManager.createFacet(AndroidFacet.getFacetType(), "Android", null);
        AndroidFacet facet = new AndroidFacet(module,"ios-facet",new AndroidFacetConfiguration());

        if (addSdk) {
            addAndroidSdk(module, sdkPath, platformDir);
        }
        final ModifiableFacetModel facetModel = facetManager.createModifiableModel();
        facetModel.addFacet(facet);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                facetModel.commit();
            }
        });
        return facet;
    }

    protected static void addAndroidSdk(Module module, String sdkPath, String platformDir) {
        Sdk androidSdk = createAndroidSdk(sdkPath, platformDir);
        ModuleRootModificationUtil.setModuleSdk(module, androidSdk);
    }

    public static Sdk createAndroidSdk(String sdkPath, String platformDir) {
        Sdk sdk = ProjectJdkTable.getInstance().createSdk("android_test_sdk", new AndroidSdkType()/*AndroidSdkType.getInstance()*/);
        SdkModificator sdkModificator = sdk.getSdkModificator();
        sdkModificator.setHomePath(sdkPath);

        LocalFileSystem inst = LocalFileSystem.getInstance();
        inst.replaceWatchedRoots(Collections.<LocalFileSystem.WatchRequest>emptySet(), Arrays.asList(new String[]{"C:\\Program Files (x86)\\Android\\android-sdk"}), null);

        VirtualFile androidJar = inst.findFileByPath(sdkPath + "/platforms/" + platformDir + "/android.jar");
        sdkModificator.addRoot(androidJar, OrderRootType.CLASSES);

        VirtualFile resFolder = LocalFileSystem.getInstance().findFileByPath(sdkPath + "/platforms/" + platformDir + "/data/res");
        sdkModificator.addRoot(resFolder, OrderRootType.CLASSES);

        VirtualFile docsFolder = LocalFileSystem.getInstance().findFileByPath(sdkPath + "/docs/reference");
        if (docsFolder != null) {
            sdkModificator.addRoot(docsFolder, JavadocOrderRootType.getInstance());
        }

        AndroidSdkAdditionalData data = new AndroidSdkAdditionalData(sdk);
        AndroidSdkData sdkData = AndroidSdkData.getSdkData(sdkPath);
        Assert.assertNotNull(sdkData);
        IAndroidTarget target = sdkData.findTargetByName("Android 5.1.1"); // TODO: Get rid of this hardcoded version number
        if (target == null) {
            IAndroidTarget[] targets = sdkData.getTargets();
            for (IAndroidTarget t : targets) {
                if (t.getLocation().contains(platformDir)) {
                    target = t;
                    break;
                }
            }
            if (target == null && targets.length > 0) {
                target = targets[targets.length - 1];
            }
        }
        Assert.assertNotNull(target);
        data.setBuildTarget(target);
        sdkModificator.setSdkAdditionalData(data);
        sdkModificator.commitChanges();
        return sdk;
    }

    protected String getResDir() {
        return "res";
    }

    public static AndroidFacet addAndroidFacet(Module module, String sdkPath, String platformDir) {
        return addAndroidFacet(module, sdkPath, platformDir, true);
    }

    protected static String getContentRootPath(@NotNull String moduleName) {
        return "/additionalModules/" + moduleName;
    }

    public static String printTree(RadViewComponent root, boolean internal) {
        StringBuilder sb = new StringBuilder(200);
        if (internal) {
            describe(sb, root, 0);
        } else {
            decorate(sb, root, 0);
        }
        System.out.println("PrintTree: " + sb.toString().trim());
        return sb.toString().trim();//.replaceAll(" ", "");
    }

    private static void decorate(StringBuilder sb, RadViewComponent component, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append("    ");
        }

        SimpleColoredRenderer renderer = new SimpleColoredRenderer();
        AndroidTreeDecorator decorator = new AndroidTreeDecorator(RadModelBuilder.getProject(component));
        decorator.decorate(component, renderer, AttributeWrapper.DEFAULT, true);
        sb.append(renderer);
        sb.append('\n');
        for (RadViewComponent child : RadViewComponent.getViewComponents(component.getChildren())) {
            decorate(sb, child, depth + 1);
        }
    }

    private static void describe(StringBuilder sb, RadViewComponent component, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append("    ");
        }
        sb.append(describe(component));
        sb.append('\n');
        for (RadViewComponent child : RadViewComponent.getViewComponents(component.getChildren())) {
            describe(sb, child, depth + 1);
        }
    }

    private static String describe(RadViewComponent root) {
        return Objects.toStringHelper(root).omitNullValues()
                .add("tag", describe(root.getTag()))
                .add("id", root.getId())
                .add("bounds", describe(root.getBounds()))
                .toString();
    }

    private static String describe(@Nullable XmlTag tag) {
        if (tag == null) {
            return "";
        } else {
            return '<' + tag.getName() + '>';
        }
    }

    private static String describe(Rectangle rectangle) {
        // More brief description than toString default: java.awt.Rectangle[x=0,y=100,width=768,height=1084]
        return "[" + rectangle.x + "," + rectangle.y + ":" + rectangle.width + "x" + rectangle.height;
    }
}
