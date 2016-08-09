package com.android.sdklib.widgets;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.sdklib.widgets.base_elements.iOSBaseElement;
import com.android.sdklib.widgets.base_elements.iOSBaseLinearLayout;
import com.android.sdklib.widgets.base_elements.iOSLinearLayout;
import com.android.sdklib.widgets.base_elements.iOSView;

public class iOSTableView extends LinearLayout implements iOSBaseElement {

    public iOSTableView(Context context) {
        super(context);
    }

    public iOSTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public iOSTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
