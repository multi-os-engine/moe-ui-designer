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

import com.intellij.psi.FileResolveScopeProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

public interface IXmlFile extends PsiFile, IXmlElement, FileResolveScopeProvider {
    IXmlFile[] EMPTY_ARRAY = new IXmlFile[0];

    @Nullable
    XmlDocument getDocument();

    @Nullable
    XmlTag getRootTag();
}