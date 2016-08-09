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

package org.moe.designer.utils;

import java.util.HashMap;
import java.util.Map;

public class IBOutletUtils {

    private static Map<String, String> returnTypeMapping;

    static {
        returnTypeMapping = new HashMap<String, String>();
        returnTypeMapping.put("com.android.sdklib.widgets.iOSButton", "UIButton");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSTextView", "UILabel");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSTextPicker", "UIPickerView");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSStepper", "UIStepper");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSSwitch", "UISwitch");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSTextField", "UITextField");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSProgressView", "UIProgressView");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSActivityIndicator", "UIActivityIndicatorView");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSSlider", "UISlider");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSSearchBar", "UISearchBar");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSDatePicker", "UIDatePicker");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSMapKitView", "MKMapView");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSSegmentedControl", "UISegmentedControl");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSPageControlGroup", "UIPageControl");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSToolBar", "UIToolbar");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSBarButtonItem", "UITabBarItem");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSTabBar", "UITabBar");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSTableView", "UITableView");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSTableViewCell", "UITableViewCell");
        returnTypeMapping.put("com.android.sdklib.widgets.iOSRadioButtonSC", "UISegmentedControlSegment");
        returnTypeMapping.put("com.android.sdklib.widgets.base_elements.iOSBaseLinearLayout", "UIView");
    }

    public static String getReturnTypeByTagName(String tagName){
        return returnTypeMapping.get(tagName);
    }

}
