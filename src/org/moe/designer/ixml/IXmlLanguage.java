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

package org.moe.designer.ixml;

import com.intellij.lang.CompositeLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class IXmlLanguage extends CompositeLanguage {
    public final static IXmlLanguage INSTANCE = new IXmlLanguage();

    private IXmlLanguage() {
        super("IXML", "text/xml");
    }

    protected IXmlLanguage(String name, String... mime) {
        super(name, mime);
    }

    protected IXmlLanguage(Language baseLanguage, String name, String... mime) {
        super(baseLanguage, name, mime);
    }
}
