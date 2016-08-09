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
import android.widget.ProgressBar;
import com.android.sdklib.widgets.base_elements.iOSBaseElement;
import com.android.sdklib.widgets.base_elements.iOSBaseProgressBar;
import com.android.sdklib.widgets.base_elements.iOSProgressBar;
import com.android.sdklib.widgets.base_elements.iOSView;

public class iOSActivityIndicator extends ProgressBar implements iOSBaseElement {

    public iOSActivityIndicator(Context context) {
        super(context);
    }

    public iOSActivityIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public iOSActivityIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public iOSActivityIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}