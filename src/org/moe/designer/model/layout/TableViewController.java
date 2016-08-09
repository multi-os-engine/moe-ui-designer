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

package org.moe.designer.model.layout;

import org.moe.designer.android.designer.LinearLayoutResizeOperation;
import org.moe.designer.android.designer.ResizeOperation;
import org.moe.designer.designSurface.TreeDropToOperation;
import org.moe.designer.designSurface.layout.LinearLayoutOperation;
import org.moe.designer.designSurface.layout.flow.FlowStaticDecorator;
import org.moe.designer.model.layout.actions.LayoutMarginOperation;
import com.intellij.designer.componentTree.TreeEditOperation;
import com.intellij.designer.designSurface.EditOperation;
import com.intellij.designer.designSurface.OperationContext;
import com.intellij.designer.designSurface.StaticDecorator;


public class TableViewController extends RadLinearLayout {
    @Override
    public EditOperation processChildOperation(OperationContext context) {
        if (context.isCreate() || context.isPaste() || context.isAdd() || context.isMove()) {
            if (context.isTree()) {
                if (TreeEditOperation.isTarget(myContainer, context)) {
                    return new TreeDropToOperation(myContainer, context);
                }
                return null;
            }
            return new LinearLayoutOperation(myContainer, context, isHorizontal());
        }
        if (context.is(ResizeOperation.TYPE)) {
            return new LinearLayoutResizeOperation(context);
        }
        if (context.is(LayoutMarginOperation.TYPE)) {
            return new LayoutMarginOperation(context);
        }
        return null;
    }
}
