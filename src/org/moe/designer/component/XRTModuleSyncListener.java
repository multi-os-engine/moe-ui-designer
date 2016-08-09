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

package org.moe.designer.component;

import org.moe.designer.android.AndroidFacetType;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;

public class XRTModuleSyncListener extends ModuleAdapter {

    public void moduleAdded(Project project, Module module) {

// TODO: temporal fix for import, because MOE libraries haven't been loaded yet when we call isValidMoeModule
// TODO:      ios facet will be added for all projects!
//        if (!UIDesignerPlugin.isValidMoeModule(module)) {
//            return;
//        }

        boolean facetFound = false;

        FacetManager manager = FacetManager.getInstance(module);

        Facet[] facets = manager.getAllFacets();

        AndroidFacetType facetType = new AndroidFacetType();
        for (Facet facet : facets) {
            if (facet.getType().getPresentableName().compareTo(facetType.getPresentableName()) == 0) {
                facetFound = true;
            }
        }

        if (!facetFound) {
            FacetUtil.addFacet(module, new AndroidFacetType());
            module.getProject().save();
        }
    }

    public void beforeModuleRemoved(final Project project, final Module module) {
    }

    public void moduleRemoved(Project project, Module module) {
    }
}
