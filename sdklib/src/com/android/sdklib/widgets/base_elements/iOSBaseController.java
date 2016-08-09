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

package com.android.sdklib.widgets.base_elements;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class iOSBaseController extends LinearLayout implements iOSView, iOSLinearLayout, iOSController {
    public iOSBaseController(Context context) {
        super(context);
    }

    public iOSBaseController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public iOSBaseController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public iOSBaseController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
