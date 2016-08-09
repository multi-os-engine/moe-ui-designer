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

package com.android.ide.common.rendering.legacy;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class CustomURLClassLoader extends URLClassLoader {
    public CustomURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public CustomURLClassLoader(URL[] urls) {
        super(urls);
    }

    public CustomURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

//    @Override
//    public Class<?> loadClass(String name) throws ClassNotFoundException{
//        if(name.contains("iOS")){
//            return loadCustomClass(name);
//        }
//        return super.loadClass(name);
//    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException{
        Class c = loadClass(name, false);

        if (c == null){
            throw new ClassNotFoundException();
        }

        return c;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);

        if(c == null && getParent() != null){
            try{
                c = getParent().loadClass(name);
            }
            catch (Throwable e){
                //nothing to do
            }

            if(c == null){
                c = loadCustomClass(name);
            }
        }
        return c;
    }

    private Class<?> loadCustomClass(String name) throws ClassNotFoundException{
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        try {
            c = findClass(name);
        }
        catch (Exception e){
//            throw new ClassNotFoundException();
        }
        return c;
    }
}
