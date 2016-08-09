package org.moe.designer.ixml;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessor;

/**
 * @author Mike
 */
public interface IXmlElement extends PsiElement {
    Key<IXmlElement> INCLUDING_ELEMENT = Key.create("INCLUDING_ELEMENT");
    Key<PsiElement> DEPENDING_ELEMENT = Key.create("DEPENDING_ELEMENT");

    IXmlElement[] EMPTY_ARRAY = new IXmlElement[0];

    boolean processElements(PsiElementProcessor processor, PsiElement place);
}
