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

import org.moe.designer.model.RadViewComponent;


public class AndroidWidgetUtils {
    public enum UnresizableElements {
        iOSSwitch("com.android.sdklib.widgets.iOSSwitch"),
        iOSNavigationBarContainer("com.android.sdklib.widgets.base_elements.iOSNavigationBarContainer"),
        iOSStepper("com.android.sdklib.widgets.iOSStepper"),
        SegmentedControlItem("com.android.sdklib.widgets.iOSRadioButtonSC"),
        PageControlButton("com.android.sdklib.widgets.iOSPageControlButton"),
        TabBarItem("com.android.sdklib.widgets.iOSTabBarItem"),
        ActivityIndicator("com.android.sdklib.widgets.iOSActivityIndicator");

        private String _tagName;
        
        UnresizableElements(String tagName){
            _tagName = tagName;
        }

        public static boolean isResizable(String tagName){
            for(UnresizableElements element : UnresizableElements.values()){
                if(element._tagName.equals(tagName)){
                    return false;
                }
            }
            return true;
        }
    }

    public enum HeightUnresizableElements {
        SegmentedControl("com.android.sdklib.widgets.iOSSegmentedControl"),
        PageControl("com.android.sdklib.widgets.iOSPageControlGroup"),
        ToolBar("com.android.sdklib.widgets.iOSToolBar"),
        ToolBarItem("com.android.sdklib.widgets.iOSBarButtonItem"),
        TabBar("com.android.sdklib.widgets.iOSTabBar"),
        Slider("com.android.sdklib.widgets.iOSSlider"),
        TextField("com.android.sdklib.widgets.iOSTextField"),
        SearchBar("com.android.sdklib.widgets.iOSSearchBar");


        private String _tagName;

        HeightUnresizableElements(String tagName){
            _tagName = tagName;
        }

        public static boolean isResizable(String tagName){
            for(HeightUnresizableElements element : HeightUnresizableElements.values()){
                if(element._tagName.equals(tagName)){
                    return false;
                }
            }
            return true;
        }
    }

    public enum WidthUnresizableElements {
        TableViewCell("com.android.sdklib.widgets.iOSTableViewCell");


        private String _tagName;

        WidthUnresizableElements(String tagName){
            _tagName = tagName;
        }

        public static boolean isResizable(String tagName){
            for(WidthUnresizableElements element : WidthUnresizableElements.values()){
                if(element._tagName.equals(tagName)){
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean isResizable(String componentTagName, String parentTagName, String attrName, boolean isHeight){
        boolean result = true;
        if(attrName == null || (attrName.equals("layout_width") || attrName.equals("layout_height"))){
            if(componentTagName.equals("com.android.sdklib.widgets.iOSTextView") && parentTagName.equals("com.android.sdklib.widgets.iOSTableView")){
                return false;
            }
            else{
                result = UnresizableElements.isResizable(componentTagName);
                if(result){
                    if(isHeight){
                        result = HeightUnresizableElements.isResizable(componentTagName);
                    }
                    else{
                        result = WidthUnresizableElements.isResizable(componentTagName);
                    }
                }
            }
        }
        return result;
    }

    public static boolean isResizable(RadViewComponent component, String attrName, boolean isHeight){
        boolean result = true;
        String tagName = component.getTag().getName();
        if(attrName == null || (attrName.equals("layout_width") || attrName.equals("layout_height"))){
            result = !isTableHeader(component);
            result &= UnresizableElements.isResizable(tagName);
            if(result){
                if(isHeight){
                    result = HeightUnresizableElements.isResizable(tagName);
                }
                else{
                    result = WidthUnresizableElements.isResizable(tagName);
                }
            }
        }
        return result;
    }

    public static boolean isResizable(RadViewComponent component, String attrName){
        boolean result = true;
        String tagName = component.getTag().getName();
        if(attrName.equals("layout_width") || attrName.equals("layout_height")){
            result = !isTableHeader(component);
            result &= UnresizableElements.isResizable(tagName);
        }
        return result;
    }

    private static boolean isTableHeader(RadViewComponent component){
        String componentTagName = component.getTag().getName();
        String parentTagName =((RadViewComponent)component.getParent()).getTag().getName();

        if(componentTagName.equals("com.android.sdklib.widgets.iOSTextView") && parentTagName.equals("com.android.sdklib.widgets.iOSTableView")){
            return true;
        }
        return false;
    }

    
}
