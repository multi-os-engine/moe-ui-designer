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

package com.android.ide.common.rendering;

import com.android.ide.common.rendering.LayoutLibrary;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.util.lang.UrlClassLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class CustomElementClassLoader extends URLClassLoader {


    public CustomElementClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException{
        if(name.contains("iOS")){
            return loadCustomClass(name);
        }
        return super.loadClass(name);
    }

    public Class<?> loadCustomClass(String name) throws ClassNotFoundException{
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }

        try {
            c = findClass(name);
        }
        catch (Exception e){
            throw new ClassNotFoundException(e.getMessage());
        }
        return c;
    }
}
