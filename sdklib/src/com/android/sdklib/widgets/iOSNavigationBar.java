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

package com.android.sdklib.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toolbar;
import com.android.sdklib.widgets.base_elements.iOSBaseToolbar;
import com.android.sdklib.widgets.base_elements.iOSLinearLayout;
import com.android.sdklib.widgets.base_elements.iOSRadioGroup;
import com.android.sdklib.widgets.base_elements.iOSView;

public class iOSNavigationBar extends Toolbar implements iOSView, iOSRadioGroup, iOSLinearLayout {
    public iOSNavigationBar(Context context) {
        super(context);
    }

    public iOSNavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public iOSNavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public iOSNavigationBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
