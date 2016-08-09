package org.moe.designer.android.dom.resources;

import org.moe.designer.android.dom.AndroidDomElement;
import org.moe.designer.android.dom.converters.QuietResourceReferenceConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;
//import org.jetbrains.android.dom.AndroidDomElement;
//import org.jetbrains.android.dom.converters.QuietResourceReferenceConverter;

/**
 * @author Eugene.Kudelevsky
 */
@Convert(QuietResourceReferenceConverter.class)
public interface ArrayElement extends AndroidDomElement, GenericDomValue {
}
